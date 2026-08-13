package net.mathias2246.buildmc.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.team.Team;
import net.mathias2246.buildmc.util.AudienceUtil;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class TeamCommand implements CustomCommand {

    @Override
    public LiteralCommandNode<CommandSourceStack> getCommand() {
        var cmd = Commands.literal("teams").requires(
                command -> command.getSender().hasPermission("buildmc.manage-teams")
        ).then(
            Commands.literal("list")
                    .executes(
                            command -> {
                                var sender = command.getSource().getSender();

                                var teams = CoreMain.teamManager.getTeams();
                                if (teams.isEmpty()) {
                                    AudienceUtil.sendMessage(
                                            sender,
                                            Component.translatable("messages.teams.no-teams-to-list")
                                            );
                                    return 1;
                                }

                                int entriesCount = 0;
                                for (var team : teams.entrySet()) {
                                    entriesCount++;
                                    if (entriesCount > 50) break;

                                    Component displayName = team.getValue().getDisplayName();
                                    if (displayName != null) {
                                        // Show the display name inside double-quotes
                                        displayName = Component.text("\"").color(null).append(displayName).append(Component.text("\"").color(null));
                                    }

                                    // When you click on the team key, show the members of the team
                                    AudienceUtil.sendMessage(
                                            sender,
                                            Component.text(team.getKey()).clickEvent(ClickEvent.runCommand("/teams list "+team.getKey())).hoverEvent(HoverEvent.showText(Component.translatable("messages.teams.team-list-hover"))).color(NamedTextColor.GRAY)
                                                    .append(displayName == null ? Component.empty() : Component.text(" - ").append(displayName))
                                                    // If the team has a color set, show a small square with that color
                                                    .append(team.getValue().getColor() == null ? Component.empty() : Component.text(" - ").color(NamedTextColor.WHITE).append(Component.text("■")).color(team.getValue().getColor()))
                                    );
                                }

                                return 1;
                            }
                    ).then(
                            Commands.argument("team", StringArgumentType.string())
                                    .executes(
                                            command -> {
                                                var sender = command.getSource().getSender();

                                                var teamKey = StringArgumentType.getString(command, "team");
                                                var team = CoreMain.teamManager.getTeam(teamKey);

                                                if (team == null) {
                                                    AudienceUtil.sendMessage(
                                                            sender,
                                                            Component.translatable("messages.teams.team-not-found")
                                                    );
                                                    return 0;
                                                }

                                                AtomicInteger count = new AtomicInteger();

                                                team.members().forEach(
                                                        member -> {
                                                            count.getAndIncrement();
                                                            var entity = Bukkit.getEntity(member);
                                                            if (entity == null)
                                                                AudienceUtil.sendMessage(
                                                                        sender,
                                                                        Component.text(count.get() + " - ").color(NamedTextColor.DARK_GRAY).append(Component.text(member.toString()).color(NamedTextColor.GRAY))
                                                                );
                                                            else AudienceUtil.sendMessage(
                                                                    sender,
                                                                    Component.text(count.get() + " - ").color(NamedTextColor.DARK_GRAY).append(entity.name().color(NamedTextColor.WHITE))
                                                            );
                                                        }
                                                );

                                                AudienceUtil.sendMessage(
                                                        sender,
                                                        Message.msg(sender, "messages.teams.team-members-count", Map.of("members", Integer.toString(count.get()), "team", teamKey))
                                                );

                                                return 1;
                                            }
                                    )
                                    .suggests(TeamCommand::suggestTeamKeys)
                    )
        ).then(
                Commands.literal("join")
                        .then(
                                Commands.argument("team", StringArgumentType.string())
                                        .suggests(TeamCommand::suggestTeamKeys)
                                        .then(
                                                Commands.argument("members", ArgumentTypes.entities())
                                                        .executes(
                                                                command -> {
                                                                    var sender = command.getSource().getSender();

                                                                    String teamKey = StringArgumentType.getString(command, "team");
                                                                    Collection<Entity> entities = command.getArgument("members", EntitySelectorArgumentResolver.class).resolve(command.getSource());

                                                                    Team team = CoreMain.teamManager.getTeam(teamKey);
                                                                    if (team == null) {
                                                                        AudienceUtil.sendMessage(
                                                                                sender,
                                                                                Component.translatable("messages.teams.team-not-found")
                                                                        );

                                                                        return 0;
                                                                    }

                                                                    int addedCount = 0;
                                                                    Component entityName = Component.text("?");
                                                                    for (Entity entity : entities) {
                                                                        if (team.addMember(entity)) {
                                                                            addedCount++;
                                                                            entityName = entity.name();
                                                                        }
                                                                    }

                                                                    if (addedCount == 1) {
                                                                        AudienceUtil.sendMessage(
                                                                                sender,
                                                                                Message.msg(
                                                                                        sender,
                                                                                        "messages.teams.added-single-entity-to-team",
                                                                                        Map.of("name", PlainTextComponentSerializer.plainText().serialize(entityName), "team", teamKey)
                                                                                )
                                                                        );
                                                                        return 1;
                                                                    }

                                                                    AudienceUtil.sendMessage(
                                                                            sender,
                                                                            Message.msg(
                                                                                    sender,
                                                                                    "messages.teams.added-entities-to-team",
                                                                                    Map.of("count", Integer.toString(addedCount), "team", teamKey)
                                                                            )
                                                                    );

                                                                    return 1;
                                                                }
                                                        )
                                        )

                        )
        ).then(
                Commands.literal("leave")
                        .then(
                                Commands.argument("team", StringArgumentType.word())
                                        .suggests(TeamCommand::suggestTeamKeysWithWildcard)
                                        .then(
                                                Commands.argument("members", ArgumentTypes.entities())
                                                        .executes(
                                                                command -> {
                                                                    var sender = command.getSource().getSender();

                                                                    String teamKey = StringArgumentType.getString(command, "team");

                                                                    Collection<Entity> entities = command.getArgument("members", EntitySelectorArgumentResolver.class).resolve(command.getSource());

                                                                    if (teamKey.equals("*")) {
                                                                        for (Entity entity : entities) {
                                                                            var team = CoreMain.teamManager.getTeamFromMember(entity);
                                                                            if (team == null) continue;

                                                                            team.removeMember(entity);
                                                                        }
                                                                    }

                                                                    Team team = CoreMain.teamManager.getTeam(teamKey);
                                                                    if (team == null) {
                                                                        AudienceUtil.sendMessage(
                                                                                sender,
                                                                                Component.translatable("messages.teams.team-not-found")
                                                                        );

                                                                        return 0;
                                                                    }

                                                                    int removedCount = 0;
                                                                    Component entityName = Component.text("?");
                                                                    for (Entity entity : entities) {
                                                                        if (team.removeMember(entity)) {
                                                                            removedCount++;
                                                                            entityName = entity.name();
                                                                        }
                                                                    }

                                                                    if (removedCount == 1) {
                                                                        AudienceUtil.sendMessage(
                                                                                sender,
                                                                                Message.msg(
                                                                                        sender,
                                                                                        "messages.teams.removed-single-entity-from-team",
                                                                                        Map.of("name", PlainTextComponentSerializer.plainText().serialize(entityName), "team", teamKey)
                                                                                )
                                                                        );
                                                                        return 1;
                                                                    }

                                                                    AudienceUtil.sendMessage(
                                                                            sender,
                                                                            Message.msg(
                                                                                    sender,
                                                                                    "messages.teams.removed-entities-from-team",
                                                                                    Map.of("count", Integer.toString(removedCount), "team", teamKey)
                                                                            )
                                                                    );

                                                                    return 1;
                                                                }
                                                        )
                                        )

                        )
        ).then(
                Commands.literal("add")
                        .then(
                                Commands.argument("team-name", StringArgumentType.string())
                                        .executes(
                                                command -> {
                                                    var sender = command.getSource().getSender();

                                                    String teamKey = StringArgumentType.getString(command, "team-name");
                                                    if (CoreMain.teamManager.isRegisteredTeam(teamKey)) {
                                                        AudienceUtil.sendMessage(
                                                                sender,
                                                                Component.translatable("messages.teams.team-already-registered")
                                                        );
                                                        return 0;
                                                    }

                                                    CoreMain.teamManager.registerTeam(
                                                            new Team(teamKey),
                                                            true // Maybe add optional parameter to set this?
                                                    );

                                                    AudienceUtil.sendMessage(
                                                            sender,
                                                            Component.translatable("messages.teams.team-registered")
                                                    );

                                                    return 1;
                                                }
                                        )
                        )
        ).then(
                Commands.literal("remove")
                        .then(
                                Commands.argument("team-name", StringArgumentType.string())
                                        .suggests(TeamCommand::suggestTeamKeys)
                                        .executes(
                                                command -> {
                                                    var sender = command.getSource().getSender();

                                                    String teamKey = StringArgumentType.getString(command, "team-name");
                                                    if (!CoreMain.teamManager.isRegisteredTeam(teamKey)) {
                                                        AudienceUtil.sendMessage(
                                                                sender,
                                                                Component.translatable("messages.teams.team-not-found")
                                                        );
                                                        return 0;
                                                    }

                                                    CoreMain.teamManager.unregisterTeam(teamKey);

                                                    AudienceUtil.sendMessage(
                                                            sender,
                                                            Component.translatable("messages.teams.team-unregistered")
                                                    );

                                                    return 1;
                                                }
                                        )
                        )
        ).then(
                Commands.literal("settings")
                        .then(
                                Commands.argument("team", StringArgumentType.string())
                                        .suggests(TeamCommand::suggestTeamKeys)
                                        .then(
                                                Commands.literal("set-color")
                                                        .then(
                                                                Commands.argument("color", ArgumentTypes.hexColor())
                                                                        .executes(
                                                                                command -> {
                                                                                    var sender = command.getSource().getSender();

                                                                                    var teamKey = StringArgumentType.getString(command, "team");
                                                                                    var team = CoreMain.teamManager.getTeam(teamKey);
                                                                                    if  (team == null) {
                                                                                        AudienceUtil.sendMessage(
                                                                                                sender,
                                                                                                Component.translatable("messages.teams.team-not-found")
                                                                                        );
                                                                                        return 0;
                                                                                    }

                                                                                    TextColor color = command.getArgument("color", TextColor.class);
                                                                                    team.setColor(color);

                                                                                    AudienceUtil.sendMessage(
                                                                                            sender,
                                                                                            Component.translatable("messages.teams.team-set-color")
                                                                                    );

                                                                                    return 1;
                                                                                }
                                                                        )
                                                        )
                                        )
                                        .then(
                                                Commands.literal("set-display-name")
                                                        .then(
                                                                Commands.argument("name-minimessage", StringArgumentType.greedyString())
                                                                        .executes(
                                                                                command -> {
                                                                                    var sender = command.getSource().getSender();

                                                                                    var teamKey = StringArgumentType.getString(command, "team");
                                                                                    var team = CoreMain.teamManager.getTeam(teamKey);
                                                                                    if  (team == null) {
                                                                                        AudienceUtil.sendMessage(
                                                                                                sender,
                                                                                                Component.translatable("messages.teams.team-not-found")
                                                                                        );
                                                                                        return 0;
                                                                                    }

                                                                                    var component = MiniMessage.miniMessage().deserialize(StringArgumentType.getString(command, "name-minimessage"));
                                                                                    team.setDisplayName(component);

                                                                                    AudienceUtil.sendMessage(
                                                                                            sender,
                                                                                            Component.translatable("messages.teams.team-set-display-name")
                                                                                    );

                                                                                    return 1;
                                                                                }
                                                                        )
                                                        )
                                        )
                        )
        );

        return cmd.build();
    }

    public static CompletableFuture<Suggestions> suggestTeamKeys(
            CommandContext<CommandSourceStack> ctx,
            SuggestionsBuilder builder
    )
    {
        for (var team : CoreMain.teamManager.getTeams().values()) {
            builder.suggest(team.getName());
        }
        return builder.buildFuture();
    }

    public static CompletableFuture<Suggestions> suggestTeamKeysWithWildcard(
            CommandContext<CommandSourceStack> ctx,
            SuggestionsBuilder builder
    )
    {
        for (var team : CoreMain.teamManager.getTeams().values()) {
            builder.suggest(team.getName());
        }
        builder.suggest("*");
        return builder.buildFuture();
    }
}
