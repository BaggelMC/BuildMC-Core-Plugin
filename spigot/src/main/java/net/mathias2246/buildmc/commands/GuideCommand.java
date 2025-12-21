package net.mathias2246.buildmc.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import net.mathias2246.buildmc.CoreMain;

public class GuideCommand implements CustomCommand {

    public CommandAPICommand getCommand() {
        var cmd = new CommandAPICommand("guide");

        cmd.executes(
                command -> {
                    command.sender().sendMessage("/guide <args>");
                    return 0;
                }
        );

        cmd.withArguments(
                new GreedyStringArgument("guide_id")
                        .executes(
                                (command) -> {
                                    return GuidesCommand.showGuide(
                                            command.sender(),
                                            command.args().getByClass("guide_id", String.class)
                                    );
                                }
                        )
                        .replaceSuggestions(
                                (info, builder) -> {
                                    String remaining = builder.getRemaining().toLowerCase();

                                    for (var ke : CoreMain.guides.keySet()) {
                                        var k = ke.toString().replace("buildmc:", "");
                                        if (k.toLowerCase().startsWith(remaining)) {
                                            builder.suggest(k);
                                        }
                                    }

                                    return builder.buildFuture();
                                }
                        )
        );

        return cmd;
    }

}

