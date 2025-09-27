package net.mathias2246.buildmc.claims;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.Main;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.claims.ClaimType;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.claims.claimSubCommands.CreateClaimSubCommand;
import net.mathias2246.buildmc.claims.claimSubCommands.WhitelistSubCommand;
import net.mathias2246.buildmc.claims.tools.ClaimSelectionTool;
import net.mathias2246.buildmc.commands.CustomCommand;
import net.mathias2246.buildmc.ui.claims.ClaimSelectMenu;
import net.mathias2246.buildmc.util.CommandUtil;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static net.mathias2246.buildmc.Main.config;

public class ClaimCommand implements CustomCommand {

    private static final @NotNull ClaimToolItemMetaModifier claimToolNameAndTooltip = new ClaimToolItemMetaModifier();
    private static ClaimSelectionTool claimTool;


    @Override
    public LiteralCommandNode<CommandSourceStack> getCommand() {
        
        var cmd = Commands.literal("claim");

        cmd.requires(
                (command) -> {
                    boolean t = config.getBoolean("claims.enabled", true);
                    if (!t) claimTool = null;
                    else claimTool = (ClaimSelectionTool) Main.customItems.get(Objects.requireNonNull(NamespacedKey.fromString("buildmc:claim_tool")).key());
                    return t;
                }
       );

        cmd.executes(ClaimCommand::handleEdit);
        cmd.then(
                Commands.literal("claimtool").executes(ClaimCommand::handleGetClaimTool)
            );
        cmd.then(
                Commands.literal("edit").executes(ClaimCommand::handleEdit)
        );
        cmd.then(
                Commands.literal("who").executes(ClaimCommand::handleWho)
        );
        cmd.then(
                Commands.literal("help").executes(ClaimCommand::handleHelp)
        );

        cmd.then(
                CreateClaimSubCommand.createSubCommand()
        );

        cmd.then(
                Commands.literal("remove")
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests(ClaimCommand::claimTypesSuggestions)
                                .then(Commands.argument("claim", StringArgumentType.word())
                                        .suggests(ClaimCommand::claimIdsSuggestions)
                                        .executes(command -> {
                                            var sender = command.getSource().getSender();
                                            if (!(CommandUtil.requiresPlayer(command) instanceof Player player)) return 0;

                                            String type = StringArgumentType.getString(command, "type").toLowerCase();
                                            String claimName = StringArgumentType.getString(command, "claim");

                                            long claimId = -1;

                                            switch (type) {
                                                case "player" -> {
                                                    List<Long> ids = ClaimManager.playerOwner.getOrDefault(player.getUniqueId(), List.of());
                                                    for (long id : ids) {
                                                        String name = ClaimManager.getClaimNameById(id);
                                                        if (name != null && name.equalsIgnoreCase(claimName)) {
                                                            claimId = id;
                                                            break;
                                                        }
                                                    }
                                                }
                                                case "team" -> {
                                                    Team team = ClaimManager.getPlayerTeam(player);
                                                    if (team == null) {
                                                        player.sendMessage(Component.translatable("messages.error.not-in-a-team"));
                                                        return 0;
                                                    }
                                                    List<Long> ids = ClaimManager.teamOwner.getOrDefault(team.getName(), List.of());
                                                    for (long id : ids) {
                                                        String name = ClaimManager.getClaimNameById(id);
                                                        if (name != null && name.equalsIgnoreCase(claimName)) {
                                                            claimId = id;
                                                            break;
                                                        }
                                                    }
                                                }
                                                case "server", "placeholder" -> {
                                                    if (!player.hasPermission("buildmc.admin")) {
                                                        player.sendMessage(Component.translatable("messages.error.no-permission"));
                                                        return 0;
                                                    }
                                                    List<Long> ids = type.equals("server") ?
                                                            ClaimManager.serverClaims : ClaimManager.placeholderClaims;
                                                    for (long id : ids) {
                                                        String name = ClaimManager.getClaimNameById(id);
                                                        if (name != null && name.equalsIgnoreCase(claimName)) {
                                                            claimId = id;
                                                            break;
                                                        }
                                                    }
                                                }
                                                default -> {
                                                    player.sendMessage(Component.translatable("messages.claims.remove.invalid-type"));
                                                    return 0;
                                                }
                                            }

                                            if (claimId == -1) {
                                                player.sendMessage(Component.translatable("messages.claims.remove.not-found"));
                                                return 0;
                                            }

                                            boolean success = ClaimManager.removeClaimById(claimId);
                                            if (success) {
                                                player.sendMessage(Component.translatable("messages.claims.remove.success"));
                                            } else {
                                                player.sendMessage(Component.translatable("messages.claims.remove.failed"));
                                            }

                                            return success ? 1 : 0;
                                        })
                                )
                        )
        );

        cmd.then(
                WhitelistSubCommand.createSubCommand()
        );
        cmd.then(
                Commands.literal("protections")
                        .requires((command) -> !CoreMain.plugin.getConfig().getBoolean("claims.hide-all-protections"))
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests(ClaimCommand::claimTypesSuggestions)
                                .then(Commands.argument("claim", StringArgumentType.word())
                                        .suggests(ClaimCommand::claimIdsSuggestions)
                                        .then(Commands.argument("key", ArgumentTypes.namespacedKey())
                                                .suggests((ctx, builder) -> {
                                                    String remaining = builder.getRemaining();
                                                    for (Protection flag : CoreMain.protectionsRegistry) {
                                                        String s = flag.getKey().toString();
                                                        if (!flag.isHidden() && s.startsWith(remaining.toLowerCase())) {
                                                            builder.suggest(s);
                                                        }
                                                    }
                                                    return builder.buildFuture();
                                                })
                                                .then(Commands.argument("value", StringArgumentType.word())
                                                        .suggests(CommandUtil::booleanSuggestion)
                                                        .executes(command -> {
                                                            var sender = command.getSource().getSender();
                                                            if (!(CommandUtil.requiresPlayer(command) instanceof Player player)) return 0;

                                                            NamespacedKey flag;

                                                            String type = StringArgumentType.getString(command, "type").toLowerCase();
                                                            String claimName = StringArgumentType.getString(command, "claim");
                                                            flag = command.getArgument("key", NamespacedKey.class);
                                                            String valueStr = StringArgumentType.getString(command, "value").toLowerCase();

                                                            boolean value;
                                                            if (valueStr.equals("true")) {
                                                                value = true;
                                                            } else if (valueStr.equals("false")) {
                                                                value = false;
                                                            } else {
                                                                player.sendMessage(Component.translatable("messages.claims.protections.invalid-value"));
                                                                return 0;
                                                            }

                                                            if (flag == null || Protection.isHiddenProtection(CoreMain.protectionsRegistry, flag)) {
                                                                player.sendMessage(Component.translatable("messages.claims.protections.invalid-flag"));
                                                                return 0;
                                                            }

                                                            Claim claim = null;
                                                            long claimId = -1;

                                                            switch (type) {
                                                                case "player" -> {
                                                                    List<Long> ids = ClaimManager.playerOwner.getOrDefault(player.getUniqueId(), List.of());
                                                                    for (long id : ids) {
                                                                        if (claimName.equalsIgnoreCase(ClaimManager.getClaimNameById(id))) {
                                                                            claim = ClaimManager.getClaimByID(id);
                                                                            claimId = id;
                                                                            break;
                                                                        }
                                                                    }
                                                                }
                                                                case "team" -> {
                                                                    Team team = ClaimManager.getPlayerTeam(player);
                                                                    if (team == null) {
                                                                        player.sendMessage(Component.translatable("messages.error.not-in-a-team"));
                                                                        return 0;
                                                                    }
                                                                    List<Long> ids = ClaimManager.teamOwner.getOrDefault(team.getName(), List.of());
                                                                    for (long id : ids) {
                                                                        if (claimName.equalsIgnoreCase(ClaimManager.getClaimNameById(id))) {
                                                                            claim = ClaimManager.getClaimByID(id);
                                                                            claimId = id;
                                                                            break;
                                                                        }
                                                                    }
                                                                }
                                                                case "server" -> {
                                                                    if (!player.hasPermission("buildmc.admin")) {
                                                                        player.sendMessage(Component.translatable("messages.error.no-permission"));
                                                                        return 0;
                                                                    }
                                                                    for (long id : ClaimManager.serverClaims) {
                                                                        if (claimName.equalsIgnoreCase(ClaimManager.getClaimNameById(id))) {
                                                                            claim = ClaimManager.getClaimByID(id);
                                                                            claimId = id;
                                                                            break;
                                                                        }
                                                                    }
                                                                }
                                                                default -> {
                                                                    player.sendMessage(Component.translatable("messages.claims.protections.invalid-type"));
                                                                    return 0;
                                                                }
                                                            }

                                                            if (claim == null || claimId == -1) {
                                                                player.sendMessage(Component.translatable("messages.claims.remove.not-found"));
                                                                return 0;
                                                            }

                                                            if (value) {
                                                                ClaimManager.addProtection(claimId, flag);
                                                                player.sendMessage(Message.msg(player, "messages.claims.protections.added", Map.of("flag", flag.toString())));
                                                            } else {
                                                                ClaimManager.removeProtection(claimId, flag);
                                                                player.sendMessage(Message.msg(player, "messages.claims.protections.removed", Map.of("flag", flag.toString())));
                                                            }

                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                        )
        );

        return cmd.build();
    }

    protected static int handleEdit(CommandContext<CommandSourceStack> command) {
        if (!(CommandUtil.requiresPlayer(command) instanceof Player player)) return 0;

        ClaimSelectMenu.open(player);
        return 1;
    }
    
    protected static int handleGetClaimTool(CommandContext<CommandSourceStack> command) {
        if (!(CommandUtil.requiresPlayer(command) instanceof Player player)) return 0;

        // Check if there is space left in the inventory
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(Component.translatable("messages.claims.tool.full-inventory"));
            return 0;
        }

        claimTool.giveToPlayer(player, claimToolNameAndTooltip);
        player.sendMessage(Component.translatable("messages.claims.tool.give-success"));
        return 1;
    }
    
    protected static int handleWho(CommandContext<CommandSourceStack> command) {
        if (!(CommandUtil.requiresPlayer(command) instanceof Player player)) return 0;

        Claim claim;

        try {
            claim = ClaimManager.getClaim(player.getLocation());
        } catch (SQLException e) {
            CoreMain.plugin.getLogger().severe("An error occurred while getting a claim from the database: " + e.getMessage());
            player.sendMessage(Component.translatable("messages.error.sql"));
            return 0;
        }

        if (claim == null) {
            command.getSource().getSender().sendMessage(Component.translatable("messages.claims.who.unclaimed"));
            return 1;
        }

        ClaimType claimType = claim.getType();

        if (claimType == ClaimType.TEAM) {
            player.sendMessage(Message.msg(player, "messages.claims.who.team-message", Map.of("owner", claim.getOwnerId())));
        } else if (claimType == ClaimType.PLAYER) {
            UUID ownerId = UUID.fromString(claim.getOwnerId());
            OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerId);
            String ownerName = owner.getName();

            if (ownerName == null) {
                ownerName = "Unknown";
            }

            player.sendMessage(Message.msg(player, "messages.claims.who.player-message", Map.of("owner", ownerName)));
        } else if (claimType == ClaimType.SERVER || claimType == ClaimType.PLACEHOLDER) {
            player.sendMessage(Message.msg(player, "messages.claims.who.server-message"));
        }

        return 1;
    }
    
    protected static int handleHelp(CommandContext<CommandSourceStack> command) {
        var sender = command.getSource().getSender();
        sender.sendMessage(Component.translatable("messages.claims.help-message"));
        return 1;
    }
    
    public static CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> claimTypesSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        List<String> suggestions = new ArrayList<>(List.of("player", "team"));

        // Add admin-only options
        if (context.getSource().getSender() instanceof Player player &&
                player.hasPermission("buildmc.admin")) {
            suggestions.add("server");
            suggestions.add("placeholder");
        }

        for (String suggestion : suggestions) {
            if (suggestion.startsWith(builder.getRemaining().toLowerCase())) {
                builder.suggest(suggestion);
            }
        }
        return builder.buildFuture();
    }

    public static CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> claimIdsSuggestions(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        var sender = ctx.getSource().getSender();
        if (!(sender instanceof Player player)) return builder.buildFuture();
        String type = StringArgumentType.getString(ctx, "type");

        if (type.equalsIgnoreCase("player")) {
            List<Long> claimIds = ClaimManager.playerOwner.getOrDefault(player.getUniqueId(), List.of());
            for (long id : claimIds) {
                String name = ClaimManager.getClaimNameById(id);
                if (name != null && name.toLowerCase().startsWith(builder.getRemaining().toLowerCase())) {
                    builder.suggest(name);
                }
            }
        } else if (type.equalsIgnoreCase("team")) {
            Team team = ClaimManager.getPlayerTeam(player);
            if (team != null) {
                List<Long> claimIds = ClaimManager.teamOwner.getOrDefault(team.getName(), List.of());
                for (long id : claimIds) {
                    String name = ClaimManager.getClaimNameById(id);
                    if (name != null && name.toLowerCase().startsWith(builder.getRemaining().toLowerCase())) {
                        builder.suggest(name);
                    }
                }
            }
        } else if (type.equalsIgnoreCase("server")) {
            if (player.hasPermission("buildmc.admin")) {
                List<Long> claimIds = ClaimManager.serverClaims;
                for (long id : claimIds) {
                    String name = ClaimManager.getClaimNameById(id);
                    if (name != null && name.toLowerCase().startsWith(builder.getRemaining().toLowerCase())) {
                        builder.suggest(name);
                    }
                }
            }
        }

        return builder.buildFuture();
    }
}
