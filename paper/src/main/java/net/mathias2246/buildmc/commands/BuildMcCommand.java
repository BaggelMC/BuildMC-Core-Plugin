package net.mathias2246.buildmc.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.mathias2246.buildmc.Main;
import net.mathias2246.buildmc.endEvent.EndEventCommand;
import net.mathias2246.buildmc.spawnElytra.ElytraZoneCommand;

public class BuildMcCommand implements CustomCommand {

    @Override
    public LiteralCommandNode<CommandSourceStack> getCommand() {
        var cmd = Commands.literal("buildmc");

        cmd.executes(
                (executionInfo -> {
                    executionInfo.getSource().getSender().sendMessage("/buildmc <args>");
                    return 1;
                })
        );

        var debugSub = Commands.literal("debug");
        debugSub.executes(
                (executionInfo) -> {
                    executionInfo.getSource().getSender().sendMessage("/buildmc debug <args>");
                    return 1;
                }
        );
        debugSub.requires(
                        (c) -> c.getSender().isOp()
                );

        var statusSub =
                Commands.literal("status")
                        .requires((command) -> command.getSender().isOp())
                    .then(
                        Commands.literal("reload")
                                .executes(
                                        (command) -> {
                                            Main.statusConfig.reload();
                                            return 1;
                                        }
                                )
        );
        cmd.then(statusSub);

        var endSub = new EndEventCommand().getCommandBuilder();
        cmd.then(endSub);

        var elytraSub = new ElytraZoneCommand().getSubCommand();
        cmd.then(elytraSub);

        cmd.then(debugSub);

        return cmd.build();
    }
}
