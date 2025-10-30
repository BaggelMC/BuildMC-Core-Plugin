package net.mathias2246.buildmc.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;

@FunctionalInterface
public interface CustomCommand {

    LiteralCommandNode<CommandSourceStack> getCommand();

}
