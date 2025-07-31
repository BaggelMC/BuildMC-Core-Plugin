package net.mathias2246.buildmc.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.mathias2246.buildmc.endEvent.EndEventCommand;
import org.bukkit.permissions.Permission;

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
                        (c) -> c.getSender().hasPermission(new Permission("buildmc.operator"))
                );

        var endSub = new EndEventCommand().getCommandBuilder();
        cmd.then(endSub);

//        var giveClaimTool = Commands.literal("claimtool");
//        giveClaimTool.executes(
//                (command) -> {
//                    if (!(command.getSource().getSender() instanceof Player player)) {
//                        command.getSource().getSender().sendMessage(Message.noPlayerErrorMsg(command.getSource().getSender()));
//                        return 0;
//                    }
//                    ClaimTool.giveToolToPlayer(player);
//                    return 1;
//                }
//        );

        // Register /buildmc sub-commands
        cmd.then(debugSub);

        return cmd.build();
    }
}
