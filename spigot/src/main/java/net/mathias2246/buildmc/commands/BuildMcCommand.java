package net.mathias2246.buildmc.commands;

import dev.jorel.commandapi.CommandAPICommand;
import net.mathias2246.buildmc.endEvent.EndEventCommand;
import org.bukkit.permissions.Permission;

import static net.mathias2246.buildmc.Main.audiences;

public class BuildMcCommand implements CustomCommand {

    @Override
    public CommandAPICommand getCommand() {
        var cmd = new CommandAPICommand("buildmc");

        cmd.executes(
                (executionInfo -> {
                    executionInfo.sender().sendMessage("/buildmc <args>");
                })
        );

        var debugSub = new CommandAPICommand(
                "debug"
        );
        debugSub.executes(
                (executionInfo) -> {
                    executionInfo.sender().sendMessage("/buildmc debug <args>");
                }
        );
        debugSub.setRequirements(
                        (c) -> c.hasPermission(new Permission("buildmc.operator"))
                );

        var endSub = new EndEventCommand(audiences).getCommand();
        cmd.withSubcommand(endSub);

        // Register /buildmc sub-commands
        cmd.withSubcommand(
                debugSub
        );

        return cmd;
    }
}
