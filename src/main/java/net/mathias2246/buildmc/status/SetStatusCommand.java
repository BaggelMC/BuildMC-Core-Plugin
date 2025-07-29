package net.mathias2246.buildmc.status;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.StringArgument;
import net.mathias2246.buildmc.commands.CustomCommand;
import org.bukkit.entity.Player;
import org.bukkit.help.HelpTopic;

public class SetStatusCommand implements CustomCommand {

    // The '/status' command
    // This command has two sub commands
    //  - 'remove': Removes your current status
    //  - 'set': Sets your current status
    @Override
    public CommandAPICommand getCommand() {
        var cmd = new CommandAPICommand("status");
        cmd.executes(
                (command) -> {}
        );
        HelpTopic help = new StatusCommandHelp();
        cmd.withHelp(
                help
        );

        cmd.withUsage(
                "remove",
                "set <status>"
        );


        var removeSub = new CommandAPICommand("remove");
        removeSub.executes(
                (command) -> {
                    if (!(command.sender() instanceof Player player)) {
                        command.sender().sendMessage("Only a player can have a status!");
                        return;
                    }
                    PlayerStatus.removePlayerStatus(player);
                }
        );

        var setSub = new CommandAPICommand("set");

        setSub.executes(
                (command) -> {
                    if (!(command.sender() instanceof Player player)) {
                        command.sender().sendMessage("Only a player can have a status!");
                        return;
                    }
                    PlayerStatus.setPlayerStatus(player, (String) command.args().get("set"));
                }
        );

        setSub.withUsage("set <status>");

        setSub.withArguments(
                new StringArgument("status")
                );
        cmd.withSubcommand(
                removeSub
        );
        cmd.withSubcommand(
                setSub
        );


        return cmd;
    }
}
