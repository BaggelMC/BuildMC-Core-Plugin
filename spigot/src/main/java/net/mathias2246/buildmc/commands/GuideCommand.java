package net.mathias2246.buildmc.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.GreedyStringArgument;

public class GuideCommand implements CustomCommand {

    public CommandAPICommand getCommand() {
        var cmd = new CommandAPICommand("guide");

        cmd.executes(
                (command) -> {
                    return GuidesCommand.showGuide(
                            command.sender(),
                            command.args().getByClass("guide_id", String.class)
                    );
                }
        );

        cmd.withArguments(
                new GreedyStringArgument("guide_id")
                        .replaceSuggestions(
                                (ctx, builder) -> GuidesCommand.buildSuggestions(builder)
                        )
        );

        return cmd;
    }

}

