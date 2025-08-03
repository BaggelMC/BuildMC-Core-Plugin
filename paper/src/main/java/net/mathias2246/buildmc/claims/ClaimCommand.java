package net.mathias2246.buildmc.claims;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.mathias2246.buildmc.commands.CustomCommand;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.io.IOException;

import static net.mathias2246.buildmc.Main.claimManager;

public class ClaimCommand implements CustomCommand {
    @Override
    public LiteralCommandNode<CommandSourceStack> getCommand() {
        var cmd = Commands.literal("claim");
        cmd.then(
                Commands.literal("claimtool")
                .executes(
                        (command) -> {
                            if (!(command.getSource().getExecutor() instanceof Player player)) {
                                command.getSource().getSender().sendMessage(Component.translatable("messages.error.not-a-player"));
                                return 0;
                            }

                            // Check if there is space left in the inventory
                            if (player.getInventory().firstEmpty() == -1) {
                                player.sendMessage(Component.translatable("messages.claims.tool.full-inventory"));
                                return 0;
                            }

                            ClaimTool.giveToolToPlayer(player);
                            player.sendMessage(Component.translatable("messages.claims.tool.give-success"));
                            return 1;
                        })
            );
        cmd.then(
                Commands.literal("removetool")
                        .executes(
                                (command) -> {
                                    if (!(command.getSource().getExecutor() instanceof Player player)) {
                                        command.getSource().getSender().sendMessage(Component.translatable("messages.error.not-a-player"));
                                        return 0;
                                    }

                                    // Check if there is space left in the inventory
                                    if (player.getInventory().firstEmpty() == -1) {
                                        player.sendMessage(Component.translatable("messages.claims.tool.full-inventory"));
                                        return 0;
                                    }

                                    ClaimTool.giveRemoveToolToPlayer(player);
                                    player.sendMessage(Component.translatable("messages.claims.tool.give-success"));
                                    return 1;
                                })
        );
        cmd.then(
                Commands.literal("who")
                        .executes(
                                (command) -> {
                                    if (!(command.getSource().getSender() instanceof Player player)) {
                                        command.getSource().getSender().sendMessage(Component.translatable("messages.error.not-a-player"));
                                        return 0;
                                    }

                                    String owner = ClaimManager.getOwnerString(player.getLocation());

                                    if (owner == null) {
                                        command.getSource().getSender().sendMessage(Component.translatable("messages.claims.who.unclaimed"));
                                        return 1;
                                    }

                                    Component message = Message.msg(player, "messages.claims.who.message");

                                    TextReplacementConfig.Builder b = TextReplacementConfig.builder();
                                    b.matchLiteral("%owner%");
                                    b.replacement(owner);

                                    command.getSource().getSender().sendMessage(message.replaceText(b.build()));
                                    return 1;
                                }
                        )
        );
        cmd.then(
                Commands.literal("help")
                        .executes(command -> {
                            var sender = command.getSource().getSender();
                            sender.sendMessage(Component.translatable("messages.claims.help-message"));
                            return 1;
                        })
        );
        cmd.then(
                Commands.literal("whitelist")
                        .then(
                                Commands.literal("add")
                                        .then(
                                                Commands.argument("player", ArgumentTypes.player())
                                                        .executes(
                                                                (command) -> {
                                                                    if (!(command.getSource().getSender() instanceof Player player)) {
                                                                        command.getSource().getSender().sendMessage(Component.translatable("messages.error.not-a-player"));
                                                                        return 0;
                                                                    }

                                                                    Team team = ClaimManager.getPlayerTeam(player);
                                                                    if (team == null) {
                                                                        command.getSource().getSender().sendMessage(Component.translatable("messages.error.not-in-a-team"));
                                                                        return 0;
                                                                    }

                                                                    var ps = command.getArgument("player", PlayerSelectorArgumentResolver.class);
                                                                    for (var p : ps.resolve(command.getSource())) {
                                                                        if (ClaimManager.isPlayerWhitelisted(claimManager, team, p)) {
                                                                            command.getSource().getSender().sendMessage(
                                                                                    Component.translatable("messages.claims.already-whitelisted")
                                                                                            .replaceText(TextReplacementConfig.builder()
                                                                                                    .matchLiteral("%player%")
                                                                                                    .replacement(p.getName())
                                                                                                    .build()));
                                                                            continue;
                                                                        }

                                                                        ClaimManager.setPlayerWhitelisted(claimManager, team, p);
                                                                        command.getSource().getSender().sendMessage(Component.translatable("messages.claims.successfully-whitelisted"));
                                                                    }

                                                                    try {
                                                                        claimManager.save();
                                                                    } catch (IOException e) {
                                                                        throw new RuntimeException(e);
                                                                    }
                                                                    return 1;
                                                                }
                                                        )
                                        )
                        )
                        .then(
                                Commands.literal("remove")
                                        .then(
                                                Commands.argument("player", ArgumentTypes.player())
                                                        .executes(
                                                                (command) -> {
                                                                    if (!(command.getSource().getSender() instanceof Player player)) {
                                                                        command.getSource().getSender().sendMessage(Component.translatable("messages.error.not-a-player"));
                                                                        return 0;
                                                                    }

                                                                    Team team = ClaimManager.getPlayerTeam(player);
                                                                    if (team == null) {
                                                                        command.getSource().getSender().sendMessage(Component.translatable("messages.error.not-in-a-team"));
                                                                        return 0;
                                                                    }

                                                                    var ps = command.getArgument("player", PlayerSelectorArgumentResolver.class);
                                                                    for (var p : ps.resolve(command.getSource())) {
                                                                        ClaimManager.removePlayerWhitelisted(claimManager, team, p);
                                                                        command.getSource().getSender().sendMessage(Component.translatable("messages.claims.successfully-removed-whitelisted"));
                                                                    }

                                                                    try {
                                                                        claimManager.save();
                                                                    } catch (IOException e) {
                                                                        throw new RuntimeException(e);
                                                                    }
                                                                    return 1;
                                                                }
                                                        )
                                        )
                        )
        );

        return cmd.build();
    }
}
