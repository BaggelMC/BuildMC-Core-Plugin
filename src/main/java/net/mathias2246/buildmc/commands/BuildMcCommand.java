package net.mathias2246.buildmc.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandAPIConfig;
import org.bukkit.command.CommandException;
import org.bukkit.permissions.Permission;

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


        // Register /buildmc sub-commands
        cmd.withSubcommand(
                debugSub
        );

        return cmd;
    }
}
