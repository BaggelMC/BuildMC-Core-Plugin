package net.mathias2246.buildmc.commands;

import dev.jorel.commandapi.CommandAPICommand;

@FunctionalInterface
public interface CustomCommand {

    CommandAPICommand getCommand();

}
