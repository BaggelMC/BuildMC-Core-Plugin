package net.mathias2246.buildmc.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class RulesCommandPlatform implements CustomCommand {
    @Override
    public LiteralCommandNode<CommandSourceStack> getCommand() {
        var cmd = Commands.literal("rules")
                .executes(ctx -> {
                    var sender = ctx.getSource().getSender();
                    return RulesCommand.execute(sender);
                });

        return cmd.build();
    }

}
