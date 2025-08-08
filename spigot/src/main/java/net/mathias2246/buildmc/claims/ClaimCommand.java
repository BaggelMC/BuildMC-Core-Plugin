package net.mathias2246.buildmc.claims;

import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.claims.tools.ClaimSelectionTool;
import net.mathias2246.buildmc.commands.CustomCommand;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static net.mathias2246.buildmc.Main.audiences;
import static net.mathias2246.buildmc.Main.customItems;

public class ClaimCommand implements CustomCommand {
    @Override
    public CommandAPICommand getCommand() {
        ClaimToolItemMetaModifier modifier = new ClaimToolItemMetaModifier();
        ClaimSelectionTool claimTool = (ClaimSelectionTool) Objects.requireNonNull(customItems.get(NamespacedKey.fromString("buildmc:claim_tool")));

        return new CommandAPICommand("claim")
                .withSubcommand(
                    new CommandAPICommand("claimtool")
                    .executes(
                            (command) -> {
                                if (!(command.sender() instanceof Player player)) {
                                    command.sender().sendMessage(Message.noPlayerErrorMsgStr(command.sender()));
                                    return;
                                }

                                // Check if there is space left in the inventory
                                if (player.getInventory().firstEmpty() == -1) {
                                    audiences.sender(player).sendMessage(Message.msg(player, "messages.claims.tool.full-inventory"));

                                    return;
                                }

                                claimTool.giveToPlayer(player, modifier);
                                audiences.sender(player).sendMessage(Message.msg(player, "messages.claims.tool.give-success"));
                            })
                )
                .withSubcommand(
                        new CommandAPICommand("who")
                                .executes(
                                        (command) -> {
                                            if (!(command.sender() instanceof Player player)) {
                                                audiences.sender(command.sender()).sendMessage(Component.translatable("messages.error.not-a-player"));
                                                return;
                                            }


                                            String owner = null;
                                            Long id = ClaimManager.getClaimId(player.getLocation().getChunk());
                                            if (id != null) {
                                                owner = ClaimManager.getClaimNameById(
                                                    id
                                                );
                                            }

                                            if (owner == null) {
                                                audiences.sender(command.sender()).sendMessage(Component.translatable("messages.claims.who.unclaimed"));
                                                return;
                                            }

                                            Component message = Message.msg(player, "messages.claims.who.message");

                                            TextReplacementConfig.Builder b = TextReplacementConfig.builder();
                                            b.matchLiteral("%owner%");
                                            b.replacement(owner);

                                            audiences.sender(command.sender()).sendMessage(message.replaceText(b.build()));
                                        }
                                )
                )

                .withSubcommand(
                        new CommandAPICommand("help")
                                .executes((command) -> {
                                    CommandSender sender = command.sender();
                                    audiences.sender(sender).sendMessage(Component.translatable("messages.claims.help-message"));
                                })
                )

                .withSubcommand(
                        new CommandAPICommand("create")
                                .withArguments(new StringArgument("type"))
                                .withArguments(new StringArgument("name"))
                                .executes((command) -> {
                                    if (!(command.sender() instanceof Player player)) {
                                        audiences.sender(command.sender()).sendMessage(Component.translatable("messages.error.not-a-player"));
                                        return 0;
                                    }

                                    String type = command.args().getByClass("type", String.class);
                                    String name = command.args().getByClass("name", String.class);

                                    if (type == null || name == null) {
                                        audiences.player(player).sendMessage(Component.translatable("messages.error.invalid-args"));
                                        return 0;
                                    }

                                    if (!player.hasMetadata(claimTool.firstSelectionKey) || !player.hasMetadata(claimTool.secondSelectionKey)) {
                                        audiences.player(player).sendMessage(Component.translatable("messages.claims.create.missing-positions"));
                                        return 0;
                                    }

                                    Location pos1 = claimTool.getFirstSelection(player);
                                    Location pos2 = claimTool.getSecondSelection(player);

                                    if (pos1 == null || pos2 == null) {
                                        audiences.player(player).sendMessage(Component.translatable("messages.claims.create.missing-positions"));
                                        return 0;
                                    }

                                    if (!Objects.equals(pos1.getWorld(), pos2.getWorld())) {
                                        audiences.player(player).sendMessage(Component.translatable("messages.claims.create.different-worlds"));
                                        return 0;
                                    }

                                    List<Claim> overlappingClaims;
                                    try {
                                        overlappingClaims = ClaimManager.getClaimsInArea(pos1, pos2);
                                    } catch (SQLException e) {
                                        CoreMain.plugin.getLogger().severe("Error while checking claim overlaps: " + e.getMessage());
                                        audiences.player(player).sendMessage(Component.translatable("messages.claims.create.error-database"));
                                        return 0;
                                    }

                                    if (!overlappingClaims.isEmpty()) {
                                        boolean serverProtected = overlappingClaims.stream()
                                                .anyMatch(claim -> claim.getType() == ClaimType.SERVER);

                                        if (serverProtected) {
                                            audiences.player(player).sendMessage(Component.translatable("messages.claims.create.protected-server"));
                                        } else {
                                            audiences.player(player).sendMessage(Component.translatable("messages.claims.create.overlap"));
                                        }
                                        return 0;
                                    }

                                    switch (type.toLowerCase()) {
                                        case "player" -> {
                                            boolean success = ClaimManager.tryClaimPlayerArea(player, name, pos1, pos2);
                                            if (success) {
                                                audiences.player(player).sendMessage(Component.translatable("messages.claims.create.success"));
                                                player.removeMetadata(claimTool.firstSelectionKey, claimTool.getPlugin());
                                                player.removeMetadata(claimTool.secondSelectionKey, claimTool.getPlugin());
                                                return 1;
                                            } else {
                                                audiences.player(player).sendMessage(Component.translatable("messages.claims.create.failed"));
                                                return 0;
                                            }
                                        }

                                        case "team" -> {
                                            Team team = ClaimManager.getPlayerTeam(player);
                                            if (team == null) {
                                                audiences.player(player).sendMessage(Component.translatable("messages.error.not-in-a-team"));
                                                return 0;
                                            }
                                            boolean success = ClaimManager.tryClaimTeamArea(team, name, pos1, pos2);
                                            if (success) {
                                                audiences.player(player).sendMessage(Component.translatable("messages.claims.create.success"));
                                                player.removeMetadata(claimTool.firstSelectionKey, claimTool.getPlugin());
                                                player.removeMetadata(claimTool.secondSelectionKey, claimTool.getPlugin());
                                                return 1;
                                            } else {
                                                audiences.player(player).sendMessage(Component.translatable("messages.claims.create.failed"));
                                                return 0;
                                            }
                                        }

                                        default -> {
                                            audiences.player(player).sendMessage(Component.translatable("messages.claims.create.invalid-type"));
                                            return 0;
                                        }
                                    }
                                })
                )

                .withSubcommand(
                        new CommandAPICommand("whitelist")
                                .withArguments(
                                        new StringArgument("action").replaceSuggestions((info, builder) -> builder.suggest("add").suggest("remove").buildFuture()),
                                        new StringArgument("type").replaceSuggestions((info, builder) -> builder.suggest("player").suggest("team").buildFuture()),
                                        new StringArgument("claim"),
                                        new StringArgument("targetPlayer")
                                                .replaceSuggestions((info, builder) -> {
                                                    CommandSender sender = info.sender();
                                                    if (!(sender instanceof Player player)) {
                                                        return builder.buildFuture();
                                                    }

                                                    List<String> inputArgs = List.of(info.currentInput().split(" "));
                                                    if (inputArgs.size() < 4) {
                                                        return builder.buildFuture();
                                                    }

                                                    String type = inputArgs.get(2);
                                                    String claimName = inputArgs.get(3);

                                                    List<Long> claimIds = switch (type.toLowerCase()) {
                                                        case "player" -> ClaimManager.playerOwner.getOrDefault(player.getUniqueId(), List.of());
                                                        case "team" -> {
                                                            Team team = ClaimManager.getPlayerTeam(player);
                                                            if (team == null) yield List.of();
                                                            yield ClaimManager.teamOwner.getOrDefault(team.getName(), List.of());
                                                        }
                                                        default -> List.of();
                                                    };

                                                    for (Long claimId : claimIds) {
                                                        String name = ClaimManager.getClaimNameById(claimId);
                                                        if (name != null && name.equalsIgnoreCase(claimName)) {
                                                            Claim claim = ClaimManager.getClaimByID(claimId);
                                                            if (claim != null) {
                                                                List<UUID> whitelisted = claim.getWhitelistedPlayers();
                                                                Team team = ClaimManager.getPlayerTeam(player);

                                                                Bukkit.getOnlinePlayers().stream()
                                                                        .filter(p -> !p.getUniqueId().equals(player.getUniqueId()))
                                                                        .filter(p -> team == null || !team.hasEntry(p.getName()))
                                                                        .filter(p -> !whitelisted.contains(p.getUniqueId()))
                                                                        .map(Player::getName)
                                                                        .forEach(builder::suggest);

                                                                break;
                                                            }
                                                        }
                                                    }

                                                    return builder.buildFuture();
                                                })

                                )
                                .executes((command) -> {
                                    if (!(command.sender() instanceof Player player)) {
                                        audiences.sender(command.sender()).sendMessage(Component.translatable("messages.error.not-a-player"));
                                        return 0;
                                    }

                                    String action = command.args().getByClass("action", String.class);
                                    String type = command.args().getByClass("type", String.class);
                                    String claimName = command.args().getByClass("claim", String.class);
                                    String targetPlayerName = command.args().getByClass("targetPlayer", String.class);

                                    if (action == null || type == null || claimName == null || targetPlayerName == null) {
                                        audiences.player(player).sendMessage(Component.translatable("messages.error.invalid-args"));
                                        return 0;
                                    }

                                    List<Long> claimIds = switch (type.toLowerCase()) {
                                        case "player" -> ClaimManager.playerOwner.getOrDefault(player.getUniqueId(), List.of());
                                        case "team" -> {
                                            Team team = ClaimManager.getPlayerTeam(player);
                                            if (team == null) {
                                                audiences.player(player).sendMessage(Component.translatable("messages.error.not-in-a-team"));
                                                yield List.of();
                                            }
                                            yield ClaimManager.teamOwner.getOrDefault(team.getName(), List.of());
                                        }
                                        default -> {
                                            audiences.player(player).sendMessage(Component.translatable("messages.claims.create.invalid-type"));
                                            yield List.of();
                                        }
                                    };

                                    Long claimId = claimIds.stream()
                                            .filter(id -> claimName.equalsIgnoreCase(ClaimManager.getClaimNameById(id)))
                                            .findFirst()
                                            .orElse(null);

                                    if (claimId == null) {
                                        audiences.player(player).sendMessage(Component.translatable("messages.claims.whitelist.claim-not-found"));
                                        return 0;
                                    }

                                    OfflinePlayer target = Bukkit.getPlayer(targetPlayerName);
                                    if (target == null) {
                                        audiences.player(player).sendMessage(Component.translatable("messages.claims.whitelist.player-not-found"));
                                        return 0;
                                    }

                                    UUID targetUUID = target.getUniqueId();
                                    Claim claim = ClaimManager.getClaimByID(claimId);
                                    if (claim == null) {
                                        audiences.player(player).sendMessage(Component.translatable("messages.claims.whitelist.claim-not-found"));
                                        return 0;
                                    }

                                    switch (action.toLowerCase()) {
                                        case "add" -> {
                                            if (claim.getWhitelistedPlayers().contains(targetUUID)) {
                                                audiences.player(player).sendMessage(Component.translatable("messages.claims.whitelist.already-added"));
                                                return 0;
                                            }
                                            ClaimManager.addPlayerToWhitelist(claimId, targetUUID);
                                            audiences.player(player).sendMessage(Component.translatable("messages.claims.whitelist.added"));
                                            return 1;
                                        }
                                        case "remove" -> {
                                            if (!claim.getWhitelistedPlayers().contains(targetUUID)) {
                                                audiences.player(player).sendMessage(Component.translatable("messages.claims.whitelist.not-on-list"));
                                                return 0;
                                            }
                                            ClaimManager.removePlayerFromWhitelist(claimId, targetUUID);
                                            audiences.player(player).sendMessage(Component.translatable("messages.claims.whitelist.removed"));
                                            return 1;
                                        }
                                        default -> {
                                            audiences.player(player).sendMessage(Component.translatable("messages.claims.whitelist.invalid-action"));
                                            return 0;
                                        }
                                    }
                                })
                )


                ;
    }
}
