package net.mathias2246.buildmc.claims;

import dev.jorel.commandapi.CommandAPICommand;
import net.mathias2246.buildmc.Main;
import net.mathias2246.buildmc.commands.CustomCommand;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
                                ClaimTool.giveToolToPlayer(player);
                            })
                )

                .withSubcommand(
                        new CommandAPICommand("help")
                                .executes((command) -> {
                                    CommandSender sender = command.sender();
                                    Main.audiences.sender(sender).sendMessage(Message.msg(sender, "messages.claims.help-message"));
                                })
                )

                .withHelp("Manage your team claims", "Use this command to manage your claims.")

                .executes(
                        (command) -> {
                            if (!(command.sender() instanceof Player player)) return;

                        }
                );
    }
}
