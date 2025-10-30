package net.mathias2246.buildmc.claims.claimSubCommands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.claims.ClaimType;
import net.mathias2246.buildmc.api.event.claims.ClaimWhitelistChangeEvent;
import net.mathias2246.buildmc.claims.ClaimLogger;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.util.CommandUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WhitelistSubCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> createSubCommand() {
        return Commands.literal("whitelist")
                .then(Commands.argument("action", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            List<String> actions = List.of("add", "remove");
                            for (String action : actions) {
                                if (action.startsWith(builder.getRemaining())) {
                                    builder.suggest(action);
                                }
                            }
                            return builder.buildFuture();
                        })
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                            List<String> suggestions = new ArrayList<>(List.of("player", "team"));

                                            // Add admin-only options
                                            if (ctx.getSource().getSender() instanceof Player player &&
                                                    player.hasPermission("buildmc.admin")) {
                                                suggestions.add("server");
                                            }

                                            for (String suggestion : suggestions) {
                                                if (suggestion.startsWith(builder.getRemaining().toLowerCase())) {
                                                    builder.suggest(suggestion);
                                                }
                                            }
                                            return builder.buildFuture();
                                        }
                                )
                                .then(Commands.argument("claim", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            var sender = ctx.getSource().getSender();
                                            if (!(sender instanceof Player player)) return builder.buildFuture();
                                            String type = StringArgumentType.getString(ctx, "type");

                                            List<Long> claimIds = List.of();

                                            if (type.equalsIgnoreCase("player")) {
                                                claimIds = ClaimManager.playerOwner.getOrDefault(player.getUniqueId(), List.of());
                                            } else if (type.equalsIgnoreCase("team")) {
                                                Team team = ClaimManager.getPlayerTeam(player);
                                                if (team != null) {
                                                    claimIds = ClaimManager.teamOwner.getOrDefault(team.getName(), List.of());
                                                }
                                            } else if (type.equalsIgnoreCase("server") && player.hasPermission("buildmc.admin")) {
                                                claimIds = ClaimManager.serverClaims;
                                            }

                                            for (long id : claimIds) {
                                                String name = ClaimManager.getClaimNameById(id);
                                                if (name != null && name.startsWith(builder.getRemaining())) {
                                                    builder.suggest(name);
                                                }
                                            }

                                            return builder.buildFuture();
                                        })
                                        .then(Commands.argument("player", StringArgumentType.word())
                                                .suggests((ctx, builder) -> {
                                                    var sender = ctx.getSource().getSender();
                                                    if (!(sender instanceof Player player)) return builder.buildFuture();
                                                    String type = StringArgumentType.getString(ctx, "type");
                                                    String claimName = StringArgumentType.getString(ctx, "claim");

                                                    Claim claim = null;
                                                    List<Long> claimIds = List.of();

                                                    if (type.equalsIgnoreCase("player")) {
                                                        claimIds = ClaimManager.playerOwner.getOrDefault(player.getUniqueId(), List.of());
                                                    } else if (type.equalsIgnoreCase("team")) {
                                                        Team team = ClaimManager.getPlayerTeam(player);
                                                        if (team != null) {
                                                            claimIds = ClaimManager.teamOwner.getOrDefault(team.getName(), List.of());
                                                        }
                                                    } else if (type.equalsIgnoreCase("server") && player.hasPermission("buildmc.admin")) {
                                                        claimIds = ClaimManager.serverClaims;
                                                    }

                                                    for (long id : claimIds) {
                                                        if (claimName.equalsIgnoreCase(ClaimManager.getClaimNameById(id))) {
                                                            claim = ClaimManager.getClaimByID(id);
                                                            break;
                                                        }
                                                    }

                                                    if (claim == null) return builder.buildFuture();

                                                    List<UUID> whitelist = claim.getWhitelistedPlayers();
                                                    boolean isServer = claim.getType() == ClaimType.SERVER;

                                                    for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                                                        if (offlinePlayer.getName() == null) continue;
                                                        if (!isServer && offlinePlayer.getUniqueId().equals(player.getUniqueId())) continue;
                                                        if (whitelist.contains(offlinePlayer.getUniqueId())) continue;
                                                        if (type.equalsIgnoreCase("team")) {
                                                            Team team = ClaimManager.getPlayerTeam(player);
                                                            if (team != null && team.hasEntry(offlinePlayer.getName())) continue;
                                                        }
                                                        if (offlinePlayer.getName().startsWith(builder.getRemaining())) {
                                                            builder.suggest(offlinePlayer.getName());
                                                        }
                                                    }

                                                    return builder.buildFuture();
                                                })
                                                .executes(command -> {
                                                    var sender = command.getSource().getSender();
                                                    if (!(CommandUtil.requiresPlayer(command) instanceof Player player)) return 0;

                                                    String action = StringArgumentType.getString(command, "action").toLowerCase();
                                                    String type = StringArgumentType.getString(command, "type").toLowerCase();
                                                    String claimName = StringArgumentType.getString(command, "claim");
                                                    String targetPlayerName = StringArgumentType.getString(command, "player");

                                                    long claimId = -1;
                                                    Claim claim = null;

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
                                                                player.sendMessage(Component.translatable("messages.claims.create.invalid-type"));
                                                                return 0;
                                                            }
                                                            List<Long> ids = ClaimManager.serverClaims;
                                                            for (long id : ids) {
                                                                if (claimName.equalsIgnoreCase(ClaimManager.getClaimNameById(id))) {
                                                                    claim = ClaimManager.getClaimByID(id);
                                                                    claimId = id;
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                        default -> {
                                                            player.sendMessage(Component.translatable("messages.claims.create.invalid-type"));
                                                            return 0;
                                                        }
                                                    }

                                                    if (claim == null || claimId == -1) {
                                                        player.sendMessage(Component.translatable("messages.claims.remove.not-found"));
                                                        return 0;
                                                    }

                                                    Player target = Bukkit.getPlayerExact(targetPlayerName);
                                                    if (target == null) {
                                                        player.sendMessage(Component.translatable("messages.claims.whitelist.player-not-found"));
                                                        return 0;
                                                    }

                                                    UUID targetUUID = target.getUniqueId();

                                                    List<UUID> whitelist = claim.getWhitelistedPlayers();

                                                    switch (action) {
                                                        case "add" -> {
                                                            if (whitelist.contains(targetUUID)) {
                                                                player.sendMessage(Component.translatable("messages.claims.whitelist.already"));
                                                            } else {
                                                                ClaimWhitelistChangeEvent event = new ClaimWhitelistChangeEvent(
                                                                        claim,
                                                                        target,
                                                                        sender,
                                                                        ClaimWhitelistChangeEvent.ChangeAction.ADDED
                                                                );
                                                                Bukkit.getPluginManager().callEvent(event);
                                                                if (event.isCancelled()) return 0;

                                                                ClaimManager.addPlayerToWhitelist(claimId, targetUUID);
                                                                player.sendMessage(Component.translatable("messages.claims.whitelist.added"));

                                                                ClaimLogger.logWhitelistAdded(player, claimName, targetPlayerName, targetUUID.toString());
                                                            }
                                                        }
                                                        case "remove" -> {
                                                            if (!whitelist.contains(targetUUID)) {
                                                                player.sendMessage(Component.translatable("messages.claims.whitelist.player-not-found"));
                                                            } else {
                                                                ClaimWhitelistChangeEvent event = new ClaimWhitelistChangeEvent(
                                                                        claim,
                                                                        target,
                                                                        sender,
                                                                        ClaimWhitelistChangeEvent.ChangeAction.REMOVED
                                                                );
                                                                Bukkit.getPluginManager().callEvent(event);
                                                                if (event.isCancelled()) return 0;

                                                                ClaimManager.removePlayerFromWhitelist(claimId, targetUUID);
                                                                player.sendMessage(Component.translatable("messages.claims.whitelist.removed"));

                                                                ClaimLogger.logWhitelistRemoved(player, claimName, targetPlayerName, targetUUID.toString());
                                                            }
                                                        }
                                                        default -> {
                                                            player.sendMessage(Component.translatable("messages.claims.whitelist.invalid-action"));
                                                            return 0;
                                                        }
                                                    }

                                                    return 1;
                                                })
                                        )
                                )
                        )
                );
    }
}
