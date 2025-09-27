package net.mathias2246.buildmc.claims.claimSubCommands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.claims.ClaimType;
import net.mathias2246.buildmc.claims.ClaimCommand;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.util.CommandUtil;
import net.mathias2246.buildmc.util.LocationUtil;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.ApiStatus;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static net.mathias2246.buildmc.CoreMain.plugin;

@ApiStatus.Internal
public final class CreateClaimSubCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> createSubCommand() {
        return Commands.literal("create")
                .then(
                        Commands.argument("type", StringArgumentType.word())
                                .suggests(ClaimCommand::claimTypesSuggestions)
                                .then(
                                        Commands.argument("name", StringArgumentType.word()) // name of claim
                                                .executes(command -> {
                                                    if (!(CommandUtil.requiresPlayer(command) instanceof Player player)) return 0;

                                                    String type = command.getArgument("type", String.class);
                                                    String name = command.getArgument("name", String.class);

                                                    // Validate positions

                                                    if (!player.hasMetadata("buildmc:claim_tool_first_selection") || !player.hasMetadata("buildmc:claim_tool_second_selection")) {
                                                        player.sendMessage(Component.translatable("messages.claims.create.missing-positions"));
                                                        return 0;
                                                    }

                                                    Location pos1 = LocationUtil.tryDeserialize(player.getMetadata("buildmc:claim_tool_first_selection").getFirst().asString());
                                                    Location pos2 = LocationUtil.tryDeserialize(player.getMetadata("buildmc:claim_tool_second_selection").getFirst().asString());

                                                    if (pos1 == null || pos2 == null) {
                                                        player.sendMessage(Component.translatable("messages.claims.create.missing-positions"));
                                                        return 0;
                                                    }

                                                    if (!pos1.getWorld().equals(pos2.getWorld())) {
                                                        player.sendMessage(Component.translatable("messages.claims.create.different-worlds"));
                                                        return 0;
                                                    }

                                                    List<Claim> overlappingClaims;
                                                    try {
                                                        overlappingClaims = ClaimManager.getClaimsInArea(pos1, pos2);
                                                    } catch (SQLException e) {
                                                        plugin.getLogger().severe("There was an error while getting the claims in an area: " + e.getMessage());
                                                        player.sendMessage(Component.translatable("messages.claims.create.error-database"));
                                                        return 0;
                                                    }

                                                    if (!overlappingClaims.isEmpty()) {
                                                        boolean serverProtected = overlappingClaims.stream()
                                                                .anyMatch(claim -> claim.getType() == ClaimType.SERVER);

                                                        if (serverProtected) {
                                                            player.sendMessage(Component.translatable("messages.claims.create.protected-server"));
                                                        } else {
                                                            player.sendMessage(Component.translatable("messages.claims.create.overlap"));
                                                        }
                                                        return 0;
                                                    }

                                                    int newClaimChunks = LocationUtil.calculateChunkArea(pos1, pos2);

                                                    switch (type.toLowerCase()) {
                                                        case "player" -> {
                                                            // Check remaining claims for player
                                                            int maxChunksAllowed = plugin.getConfig().getInt("claims.player-max-chunk-claim-amount");
                                                            int currentClaimedChunks = ClaimManager.playerRemainingClaims.getOrDefault(player.getUniqueId().toString(), maxChunksAllowed);
                                                            if ((currentClaimedChunks - newClaimChunks) <= 0) {
                                                                player.sendMessage(Message.msg(player, "messages.claims.create.no-remaining-claims", Map.of("no-remaining-claims", String.valueOf(currentClaimedChunks))));
                                                                return 0;
                                                            }

                                                            try {
                                                                if (ClaimManager.doesOwnerHaveClaimWithName(player.getUniqueId().toString(), name)) {
                                                                    player.sendMessage(Component.translatable("messages.claims.create.duplicate-name"));
                                                                    return 1;
                                                                }
                                                            } catch (SQLException e) {
                                                                plugin.getLogger().severe("SQL Error while trying to check claim name availability: " + e);
                                                                player.sendMessage(Component.translatable("messages.claims.create.error-database"));
                                                                return 0;
                                                            }

                                                            boolean success = ClaimManager.tryClaimPlayerArea(player, name, pos1, pos2);
                                                            if (success) {
                                                                player.sendMessage(Message.msg(player, "messages.claims.create.success", Map.of("remaining_claims", String.valueOf(currentClaimedChunks - newClaimChunks))));
                                                                removeSelectionData(player);
                                                                return 1;
                                                            } else {
                                                                player.sendMessage(Component.translatable("messages.claims.create.failed"));
                                                                return 0;
                                                            }
                                                        }

                                                        case "team" -> {
                                                            Team team = ClaimManager.getPlayerTeam(player);
                                                            if (team == null) {
                                                                player.sendMessage(Component.translatable("messages.error.not-in-a-team"));
                                                                return 0;
                                                            }

                                                            // Check remaining claims for team
                                                            int maxChunksAllowed = plugin.getConfig().getInt("claims.team-max-chunk-claim-amount");
                                                            int currentClaimedChunks = ClaimManager.teamRemainingClaims.getOrDefault(team.getName(), maxChunksAllowed);
                                                            if ((currentClaimedChunks - newClaimChunks) <= 0) {
                                                                player.sendMessage(Message.msg(player, "messages.claims.create.no-remaining-claims", Map.of("remaining_claims", String.valueOf(currentClaimedChunks))));
                                                                return 0;
                                                            }

                                                            try {
                                                                if (ClaimManager.doesOwnerHaveClaimWithName(team.getName(), name)) {
                                                                    player.sendMessage(Component.translatable("messages.claims.create.duplicate-name"));
                                                                    return 1;
                                                                }
                                                            } catch (SQLException e) {
                                                                plugin.getLogger().severe("SQL Error while trying to check claim name availability: " + e);
                                                                player.sendMessage(Component.translatable("messages.claims.create.error-database"));
                                                                return 0;
                                                            }

                                                            boolean success = ClaimManager.tryClaimTeamArea(team, name, pos1, pos2);
                                                            if (success) {
                                                                player.sendMessage(Message.msg(player, "messages.claims.create.success", Map.of("remaining_claims", String.valueOf(currentClaimedChunks - newClaimChunks))));
                                                                removeSelectionData(player);
                                                                return 1;
                                                            } else {
                                                                player.sendMessage(Component.translatable("messages.claims.create.failed"));
                                                                return 0;
                                                            }
                                                        }

                                                        case "server", "placeholder" -> {
                                                            if (!player.hasPermission("buildmc.admin")) {
                                                                player.sendMessage(Component.translatable("messages.error.no-permission"));
                                                                return 0;
                                                            }

                                                            try {
                                                                if (ClaimManager.doesOwnerHaveClaimWithName(type.toLowerCase(), name)) {
                                                                    player.sendMessage(Component.translatable("messages.claims.create.duplicate-name"));
                                                                    return 1;
                                                                }
                                                            } catch (SQLException e) {
                                                                plugin.getLogger().severe("SQL Error while trying to check claim name availability: " + e);
                                                                player.sendMessage(Component.translatable("messages.claims.create.error-database"));
                                                                return 0;
                                                            }

                                                            boolean success = switch (type.toLowerCase()) {
                                                                case "server" -> ClaimManager.tryClaimServerArea(name, pos1, pos2);
                                                                case "placeholder" -> ClaimManager.tryClaimPlaceholderArea(name, pos1, pos2);
                                                                default -> false;
                                                            };

                                                            if (success) {
                                                                player.sendMessage(Message.msg(player,
                                                                        "messages.claims.create.success-" + type.toLowerCase(),
                                                                        Map.of("claim_name", name)
                                                                ));
                                                                removeSelectionData(player);
                                                                return 1;
                                                            } else {
                                                                player.sendMessage(Component.translatable("messages.claims.create.failed"));
                                                                return 0;
                                                            }
                                                        }

                                                        default -> {
                                                            player.sendMessage(Component.translatable("messages.claims.create.invalid-type"));
                                                            return 0;
                                                        }
                                                    }
                                                })
                                )
                );
    }

    private static void removeSelectionData(Player player) {
        player.removeMetadata("buildmc:claim_tool_first_selection", plugin);
        player.removeMetadata("buildmc:claim_tool_second_selection", plugin);
    }
}
