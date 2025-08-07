package net.mathias2246.buildmc.claims;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.Main;
import net.mathias2246.buildmc.claims.tools.ClaimSelectionTool;
import net.mathias2246.buildmc.commands.CustomCommand;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ClaimCommand implements CustomCommand {

    private static final @NotNull ClaimToolItemMetaModifier claimToolNameAndTooltip = new ClaimToolItemMetaModifier();
    private static ClaimSelectionTool claimTool;

    @Override
    public LiteralCommandNode<CommandSourceStack> getCommand() {
        claimTool = (ClaimSelectionTool) Main.customItems.get(Objects.requireNonNull(NamespacedKey.fromString("buildmc:claim_tool")).key());

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

                            claimTool.giveToPlayer(player, claimToolNameAndTooltip);
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
                Commands.literal("create")
                        .then(
                                Commands.argument("type", StringArgumentType.word()) // "player" or "team"
                                        .then(
                                                Commands.argument("name", StringArgumentType.word()) // name of claim
                                                        .executes(command -> {
                                                            if (!(command.getSource().getSender() instanceof Player player)) {
                                                                command.getSource().getSender().sendMessage(Component.translatable("messages.error.not-a-player"));
                                                                return 0;
                                                            }

                                                            String type = command.getArgument("type", String.class);
                                                            String name = command.getArgument("name", String.class);

                                                            // Validate positions

                                                            if (!player.hasMetadata(claimTool.firstSelectionKey) || !player.hasMetadata(claimTool.secondSelectionKey)) {
                                                                player.sendMessage(Component.translatable("messages.claims.create.missing-positions"));
                                                                return 0;
                                                            }

                                                            Location pos1 = claimTool.getFirstSelection(player);
                                                            Location pos2 = claimTool.getSecondSelection(player);

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
                                                                CoreMain.plugin.getLogger().severe("There was an error while getting the claims in an area: " + e.getMessage());
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

                                                            switch (type.toLowerCase()) {
                                                                case "player" -> {
                                                                    boolean success = ClaimManager.tryClaimPlayerArea(player, name, pos1, pos2);
                                                                    if (success) {
                                                                        player.sendMessage(Component.translatable("messages.claims.create.success"));
                                                                        player.removeMetadata(claimTool.firstSelectionKey, claimTool.getPlugin());
                                                                        player.removeMetadata(claimTool.secondSelectionKey, claimTool.getPlugin());
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
                                                                    boolean success = ClaimManager.tryClaimTeamArea(team, name, pos1, pos2);
                                                                    if (success) {
                                                                        player.sendMessage(Component.translatable("messages.claims.create.success"));
                                                                        player.removeMetadata(claimTool.firstSelectionKey, claimTool.getPlugin());
                                                                        player.removeMetadata(claimTool.secondSelectionKey, claimTool.getPlugin());
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
                        )
        );

        cmd.then(
                Commands.literal("remove")
                        .then(Commands.argument("type", StringArgumentType.word())
                                .then(Commands.argument("owner", StringArgumentType.word())
                                        .then(Commands.argument("name", StringArgumentType.word())
                                                .executes(command -> {
                                                    if (!(command.getSource().getSender() instanceof Player player)) {
                                                        command.getSource().getSender().sendMessage(Component.translatable("messages.error.not-a-player"));
                                                        return 0;
                                                    }

                                                    String type = command.getArgument("type", String.class).toLowerCase();
                                                    String ownerInput = command.getArgument("owner", String.class);
                                                    String name = command.getArgument("name", String.class);

                                                    long claimIdToRemove = -1;

                                                    try {
                                                        switch (type) {
                                                            case "player" -> {
                                                                OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerInput);
                                                                if (!ClaimManager.playerOwner.containsKey(owner.getUniqueId())) {
                                                                    player.sendMessage(Component.translatable("messages.claims.remove.not-found"));
                                                                    return 0;
                                                                }
                                                                for (long id : ClaimManager.playerOwner.get(owner.getUniqueId())) {
                                                                    String claimName = ClaimManager.getClaimNameById(id);
                                                                    if (claimName != null && claimName.equalsIgnoreCase(name)) {
                                                                        claimIdToRemove = id;
                                                                        break;
                                                                    }
                                                                }
                                                            }

                                                            case "team" -> {
                                                                if (!ClaimManager.teamOwner.containsKey(ownerInput)) {
                                                                    player.sendMessage(Component.translatable("messages.claims.remove.not-found"));
                                                                    return 0;
                                                                }
                                                                for (long id : ClaimManager.teamOwner.get(ownerInput)) {
                                                                    String claimName = ClaimManager.getClaimNameById(id);
                                                                    if (claimName != null && claimName.equalsIgnoreCase(name)) {
                                                                        claimIdToRemove = id;
                                                                        break;
                                                                    }
                                                                }
                                                            }

                                                            case "server" -> {
                                                                if (!player.hasPermission("buildmc.operator") && !player.hasPermission("buildmc.admin")) {
                                                                    player.sendMessage(Component.translatable("messages.error.no-permission"));
                                                                    return 0;
                                                                }
                                                                for (long id : ClaimManager.serverOwner) {
                                                                    String claimName = ClaimManager.getClaimNameById(id);
                                                                    if (claimName != null && claimName.equalsIgnoreCase(name)) {
                                                                        claimIdToRemove = id;
                                                                        break;
                                                                    }
                                                                }
                                                            }

                                                            default -> {
                                                                player.sendMessage(Component.translatable("messages.claims.remove.invalid-type"));
                                                                return 0;
                                                            }
                                                        }

                                                        if (claimIdToRemove == -1) {
                                                            player.sendMessage(Component.translatable("messages.claims.remove.not-found"));
                                                            return 0;
                                                        }

                                                        boolean success = ClaimManager.removeClaimById(claimIdToRemove);
                                                        if (success) {
                                                            player.sendMessage(Component.translatable("messages.claims.remove.success"));
                                                            return 1;
                                                        } else {
                                                            player.sendMessage(Component.translatable("messages.claims.remove.failed"));
                                                            return 0;
                                                        }

                                                    } catch (Exception e) {
                                                        CoreMain.plugin.getLogger().severe("Failed to remove claim: " + e.getMessage());
                                                        player.sendMessage(Component.translatable("messages.claims.remove.error"));
                                                        return 0;
                                                    }
                                                })
                                        )
                                )
                        )
        );


        return cmd.build();
    }
}
