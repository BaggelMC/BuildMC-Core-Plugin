package net.mathias2246.buildmc.claims;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.mathias2246.buildmc.commands.CustomCommand;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.io.IOException;

import static net.mathias2246.buildmc.CoreMain.audiences;
import static net.mathias2246.buildmc.CoreMain.claimManager;

public class ClaimCommand implements CustomCommand {
    @Override
    public CommandAPICommand getCommand() {
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

                                ClaimTool.giveToolToPlayer(player);
                                audiences.sender(player).sendMessage(Message.msg(player, "messages.claims.tool.give-success"));
                            })
                )
                .withSubcommand(
                        new CommandAPICommand("removetool")
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

                                            ClaimTool.giveRemoveToolToPlayer(player);
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



                                            String owner = ClaimManager.getOwnerString(player.getLocation());

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
                        new CommandAPICommand("whitelist")
                                .withSubcommand(
                                        new CommandAPICommand("add")
                                                .withArguments(new PlayerArgument("player"))
                                                .executes((command) -> {
                                                    if (!(command.sender() instanceof Player player)) {
                                                        audiences.sender(command.sender()).sendMessage(Component.translatable("messages.error.not-a-player"));
                                                        return;
                                                    }

                                                    Team team = ClaimManager.getPlayerTeam(player);
                                                    if (team == null) {
                                                        audiences.sender(command.sender()).sendMessage(Component.translatable("messages.error.not-in-a-team"));
                                                        return;
                                                    }

                                                    Player p = command.args().getByClass("player", Player.class);
                                                    if (p == null) return;

                                                    if (ClaimManager.isPlayerWhitelisted(claimManager, team, p)) {
                                                        audiences.sender(command.sender()).sendMessage(Component.translatable("messages.claims.already-whitelisted"));
                                                        return;
                                                    }

                                                    ClaimManager.setPlayerWhitelisted(claimManager, team, p);
                                                    audiences.sender(command.sender()).sendMessage(Component.translatable("messages.claims.successfully-whitelisted"));

                                                    try {
                                                        claimManager.save();
                                                    } catch (IOException e) {
                                                        throw new RuntimeException(e);
                                                    }
                                                })
                                )
                                .withSubcommand(
                                        new CommandAPICommand("remove")
                                                .withArguments(new PlayerArgument("player"))
                                                .executes((command) -> {
                                                    if (!(command.sender() instanceof Player player)) {
                                                        audiences.sender(command.sender()).sendMessage(Component.translatable("messages.error.not-a-player"));
                                                        return;
                                                    }

                                                    Team team = ClaimManager.getPlayerTeam(player);
                                                    if (team == null) {
                                                        audiences.sender(command.sender()).sendMessage(Component.translatable("messages.error.not-in-a-team"));
                                                        return;
                                                    }

                                                    Player p = command.args().getByClass("player", Player.class);
                                                    if (p == null) return;

                                                    ClaimManager.removePlayerWhitelisted(claimManager, team, p);
                                                    audiences.sender(command.sender()).sendMessage(Component.translatable("messages.claims.successfully-whitelisted"));

                                                    try {
                                                        claimManager.save();
                                                    } catch (IOException e) {
                                                        throw new RuntimeException(e);
                                                    }
                                                })
                                )
                )

                .executes(
                        (command) -> {
                        }
                );
    }
}
