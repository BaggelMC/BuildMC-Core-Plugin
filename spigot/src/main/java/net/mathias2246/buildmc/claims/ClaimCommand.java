package net.mathias2246.buildmc.claims;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.claims.tools.ClaimSelectionTool;
import net.mathias2246.buildmc.commands.CustomCommand;
import net.mathias2246.buildmc.ui.claims.ClaimSelectMenu;
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

import static net.mathias2246.buildmc.Main.audiences;
import static net.mathias2246.buildmc.Main.customItems;

public class ClaimCommand implements CustomCommand {
    @Override
    public CommandAPICommand getCommand() {
        ClaimToolItemMetaModifier modifier = new ClaimToolItemMetaModifier();
        ClaimSelectionTool claimTool = (ClaimSelectionTool) Objects.requireNonNull(customItems.get(NamespacedKey.fromString("buildmc:claim_tool")));

        return new CommandAPICommand("claim")

                .executes(
                        (command) -> {
                            if (!(command.sender() instanceof Player player)) {
                                command.sender().sendMessage(Message.noPlayerErrorMsgStr(command.sender()));
                                return;
                            }

                            ClaimSelectMenu.open(player);
                        })

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
                        new CommandAPICommand("edit")
                                .executes(
                                        (command) -> {
                                            if (!(command.sender() instanceof Player player)) {
                                                command.sender().sendMessage(Message.noPlayerErrorMsgStr(command.sender()));
                                                return;
                                            }

                                            ClaimSelectMenu.open(player);
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

                                            try {
                                                if (ClaimManager.doesOwnerHaveClaimWithName(player.getUniqueId().toString(), name)) {
                                                    audiences.player(player).sendMessage(Component.translatable("messages.claims.create.duplicate-name"));
                                                    return 1;
                                                }
                                            } catch (SQLException e) {
                                                CoreMain.plugin.getLogger().severe("SQL Error while trying to check claim name availability: " + e);
                                                audiences.player(player).sendMessage(Component.translatable("messages.claims.create.error-database"));
                                                return 0;
                                            }

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

                                            try {
                                                if (ClaimManager.doesOwnerHaveClaimWithName(team.getName(), name)) {
                                                    audiences.player(player).sendMessage(Component.translatable("messages.claims.create.duplicate-name"));
                                                    return 1;
                                                }
                                            } catch (SQLException e) {
                                                CoreMain.plugin.getLogger().severe("SQL Error while trying to check claim name availability: " + e);
                                                audiences.player(player).sendMessage(Component.translatable("messages.claims.create.error-database"));
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
                .withSubcommand(
                        new CommandAPICommand("protections")
                                .withArguments(
                                        new StringArgument("type")
                                                .replaceSuggestions((info, builder) -> builder.suggest("player").suggest("team").buildFuture()),
                                        new StringArgument("claim")
                                                .replaceSuggestions((info, builder) -> {
                                                    if (!(info.sender() instanceof Player player)) {
                                                        return builder.buildFuture();
                                                    }

                                                    String type = info.previousArgs().getOrDefault("type", "").toString().toLowerCase();
                                                    List<Long> claimIds = switch (type) {
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
                                                        if (name != null) builder.suggest(name);
                                                    }

                                                    return builder.buildFuture();
                                                }),
                                        new StringArgument("flag")
                                                .replaceSuggestions((info, builder) -> {
                                                    for (ProtectionFlag flag : ProtectionFlag.values()) {
                                                        builder.suggest(flag.name());
                                                    }
                                                    return builder.buildFuture();
                                                }),
                                        new StringArgument("value")
                                                .replaceSuggestions((info, builder) -> builder.suggest("true").suggest("false").buildFuture())
                                )
                                .executes((command) -> {
                                    if (!(command.sender() instanceof Player player)) {
                                        audiences.sender(command.sender()).sendMessage(Component.translatable("messages.error.not-a-player"));
                                        return 0;
                                    }

                                    String type = command.args().getByClass("type", String.class);
                                    String claimName = command.args().getByClass("claim", String.class);
                                    String flagName = command.args().getByClass("flag", String.class);
                                    String value = command.args().getByClass("value", String.class);

                                    if (type == null || claimName == null || flagName == null || value == null) {
                                        audiences.player(player).sendMessage(Component.translatable("messages.error.invalid-args"));
                                        return 0;
                                    }

                                    // --- Check type ---
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
                                            audiences.player(player).sendMessage(Component.translatable("messages.claims.protections.invalid-type"));
                                            yield List.of();
                                        }
                                    };

                                    if (claimIds.isEmpty()) {
                                        return 0;
                                    }

                                    Long claimId = claimIds.stream()
                                            .filter(id -> claimName.equalsIgnoreCase(ClaimManager.getClaimNameById(id)))
                                            .findFirst()
                                            .orElse(null);

                                    if (claimId == null) {
                                        audiences.player(player).sendMessage(Component.translatable("messages.claims.protections.not-found"));
                                        return 0;
                                    }

                                    // --- Check flag ---
                                    ProtectionFlag flag;
                                    try {
                                        flag = ProtectionFlag.valueOf(flagName.toUpperCase());
                                    } catch (IllegalArgumentException e) {
                                        audiences.player(player).sendMessage(Component.translatable("messages.claims.protections.invalid-flag"));
                                        return 0;
                                    }

                                    // --- Check value ---
                                    boolean enable;
                                    if (value.equalsIgnoreCase("true")) {
                                        enable = true;
                                    } else if (value.equalsIgnoreCase("false")) {
                                        enable = false;
                                    } else {
                                        audiences.player(player).sendMessage(Component.translatable("messages.claims.protections.invalid-value"));
                                        return 0;
                                    }

                                    Claim claim = ClaimManager.getClaimByID(claimId);
                                    if (claim == null) {
                                        audiences.player(player).sendMessage(Component.translatable("messages.claims.protections.not-found"));
                                        return 0;
                                    }

                                    if (enable) {
                                        ClaimManager.addProtectionFlag(claim, flag);
                                        audiences.player(player).sendMessage(
                                                Message.msg(player, "messages.claims.protections.added")
                                                        .replaceText(TextReplacementConfig.builder().matchLiteral("%flag%").replacement(flag.name()).build())
                                        );
                                    } else {
                                        ClaimManager.removeProtectionFlag(claim, flag);
                                        audiences.player(player).sendMessage(
                                                Message.msg(player, "messages.claims.protections.removed")
                                                        .replaceText(TextReplacementConfig.builder().matchLiteral("%flag%").replacement(flag.name()).build())
                                        );
                                    }

                                    return 1;
                                })
                )



                ;
    }
}
