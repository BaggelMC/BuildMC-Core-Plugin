package net.mathias2246.buildmc.claims;

import dev.jorel.commandapi.CommandAPICommand;
import net.mathias2246.buildmc.commands.CustomCommand;
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
                                    command.sender().sendMessage("Only Players can use this command!");
                                    return;
                                }
                                ClaimTool.giveToolToPlayer(player);
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
