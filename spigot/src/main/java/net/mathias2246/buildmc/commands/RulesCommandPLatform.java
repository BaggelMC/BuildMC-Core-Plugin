package net.mathias2246.buildmc.commands;

import dev.jorel.commandapi.CommandAPICommand;

public class RulesCommandPLatform implements CustomCommand{
    @Override
    public CommandAPICommand getCommand() {

        return new CommandAPICommand("rules")
                .executes((sender, args) -> {
                    return  RulesCommand.execute(sender);
                });
    }
}
