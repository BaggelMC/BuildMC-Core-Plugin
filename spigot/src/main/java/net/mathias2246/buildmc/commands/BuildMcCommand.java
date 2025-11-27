package net.mathias2246.buildmc.commands;

import dev.jorel.commandapi.CommandAPICommand;
import net.mathias2246.buildmc.Main;
import net.mathias2246.buildmc.endEvent.EndEventCommand;
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
                        (c) -> c.hasPermission(new Permission("buildmc.debug"))
                );

        var statusSub = new CommandAPICommand("status");
        statusSub.setRequirements(
                sender -> sender.hasPermission("buildmc.admin")
        );

        statusSub.withSubcommand(
                new CommandAPICommand("reload").executes(
                        (command) -> {
                            Main.statusConfig.reload();
                        }
                )
        );
        cmd.withSubcommand(statusSub);

        var endSub = new EndEventCommand().getCommand();
        cmd.withSubcommand(endSub);

        // Register /buildmc sub-commands
        cmd.withSubcommand(
                debugSub
        );

        return cmd;
    }
}
