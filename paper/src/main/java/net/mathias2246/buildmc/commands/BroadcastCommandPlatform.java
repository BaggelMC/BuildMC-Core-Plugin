package net.mathias2246.buildmc.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class BroadcastCommandPlatform implements CustomCommand {

    @Override
    public LiteralCommandNode<CommandSourceStack> getCommand() {
        var cmd = Commands.literal("broadcast")
                .requires(c -> c.getSender().hasPermission("buildmc.broadcast"))
                .then(
                        Commands.argument("message", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    var sender = ctx.getSource().getSender();
                                    String input = StringArgumentType.getString(ctx, "message");

                                    return BroadcastCommand.execute(sender, input);
                                })
                );

        return cmd.build();
    }
}
