package net.mathias2246.buildmc.claims;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.claims.tools.ClaimSelectionTool;
import net.mathias2246.buildmc.commands.CustomCommand;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

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
                                .withArguments(
                                        new StringArgument("type")
                                                .then(
                                                        new StringArgument("name")
                                                                .executes(
                                                                        (command) -> {
                                                                            if (!(command.sender() instanceof Player player)) {
                                                                                audiences.sender(command.sender()).sendMessage(Component.translatable("messages.error.not-a-player"));
                                                                                return 0;
                                                                            }

                                                                            String type = command.args().getByClass("type", String.class);
                                                                            String name = command.args().getByClass("name", String.class);

                                                                            if (type == null || name == null) {
                                                                                // TODO: Message for invalid params
                                                                                return 0;
                                                                            }

                                                                            // Validate positions

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
                                                                                CoreMain.plugin.getLogger().severe("There was an error while getting the claims in an area: " + e.getMessage());
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
                                                                        }
                                                                )
                                                )
                                )
                )

//                .withSubcommand(
//                        new CommandAPICommand("whitelist")
//                                .withSubcommand(
//                                        new CommandAPICommand("add")
//                                                .withArguments(new PlayerArgument("player"))
//                                                .executes((command) -> {
//                                                    if (!(command.sender() instanceof Player player)) {
//                                                        audiences.sender(command.sender()).sendMessage(Component.translatable("messages.error.not-a-player"));
//                                                        return;
//                                                    }
//
//                                                    Team team = ClaimManager.getPlayerTeam(player);
//                                                    if (team == null) {
//                                                        audiences.sender(command.sender()).sendMessage(Component.translatable("messages.error.not-in-a-team"));
//                                                        return;
//                                                    }
//
//                                                    Player p = command.args().getByClass("player", Player.class);
//                                                    if (p == null) return;
//
//                                                    Long id = ClaimManager.getClaimId()
//
//                                                    ClaimManager.addPlayerToWhitelist();
//
//                                                    if (ClaimManager.(claimManager, team, p)) {
//                                                        audiences.sender(command.sender()).sendMessage(Component.translatable("messages.claims.already-whitelisted"));
//                                                        return;
//                                                    }
//
//                                                    ClaimManager.setPlayerWhitelisted(claimManager, team, p);
//                                                    audiences.sender(command.sender()).sendMessage(Component.translatable("messages.claims.successfully-whitelisted"));
//
//                                                    try {
//                                                        claimManager.save();
//                                                    } catch (IOException e) {
//                                                        throw new RuntimeException(e);
//                                                    }
//                                                })
//                                )
//                                .withSubcommand(
//                                        new CommandAPICommand("remove")
//                                                .withArguments(new PlayerArgument("player"))
//                                                .executes((command) -> {
//                                                    if (!(command.sender() instanceof Player player)) {
//                                                        audiences.sender(command.sender()).sendMessage(Component.translatable("messages.error.not-a-player"));
//                                                        return;
//                                                    }
//
//                                                    Team team = ClaimManager.getPlayerTeam(player);
//                                                    if (team == null) {
//                                                        audiences.sender(command.sender()).sendMessage(Component.translatable("messages.error.not-in-a-team"));
//                                                        return;
//                                                    }
//
//                                                    Player p = command.args().getByClass("player", Player.class);
//                                                    if (p == null) return;
//
//                                                    ClaimManager.removePlayerWhitelisted(claimManager, team, p);
//                                                    audiences.sender(command.sender()).sendMessage(Component.translatable("messages.claims.successfully-whitelisted"));
//
//                                                    try {
//                                                        claimManager.save();
//                                                    } catch (IOException e) {
//                                                        throw new RuntimeException(e);
//                                                    }
//                                                })
//                                )
//                )
        ;
    }
}
