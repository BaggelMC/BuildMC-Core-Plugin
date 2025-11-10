package net.mathias2246.buildmc.claims.claimSubCommands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.claims.ClaimType;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.commands.claim.ClaimWhitelist;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.mathias2246.buildmc.commands.CommandUtil.requiresPlayer;

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
                                                    if (!(requiresPlayer(sender) instanceof Player player)) return 0;

                                                    String action = StringArgumentType.getString(command, "action").toLowerCase();
                                                    String type = StringArgumentType.getString(command, "type").toLowerCase();
                                                    String claimName = StringArgumentType.getString(command, "claim");
                                                    String targetPlayerName = StringArgumentType.getString(command, "player");

                                                    return ClaimWhitelist.whitelistClaimCommand(player, type, claimName, action, targetPlayerName);
                                                })
                                        )
                                )
                        )
                );
    }
}
