package net.mathias2246.buildmc.commands;

import dev.jorel.commandapi.CommandAPICommand;
import net.mathias2246.buildmc.claims.ClaimTool;
import net.mathias2246.buildmc.endEvent.EndEventCommand;
import org.bukkit.entity.Player;
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

        var endSub = new EndEventCommand().getCommand();
        cmd.withSubcommand(endSub);

        var giveClaimTool = new CommandAPICommand("claimtool");
        giveClaimTool.executes(
                (command) -> {
                    if (!(command.sender() instanceof Player player)) {
                        command.sender().sendMessage("Only Players can use this command!");
                        return;
                    }
                    ClaimTool.giveToolToPlayer(player);
                }
        );

        // Register /buildmc sub-commands
        cmd.withSubcommand(
                debugSub
        );
        cmd.withSubcommand(
                giveClaimTool
        );

        return cmd;
    }
}
