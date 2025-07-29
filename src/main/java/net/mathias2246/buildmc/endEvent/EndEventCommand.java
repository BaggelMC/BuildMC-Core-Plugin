package net.mathias2246.buildmc.endEvent;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.BooleanArgument;
import net.mathias2246.buildmc.commands.CustomCommand;

import java.io.IOException;

import static net.mathias2246.buildmc.Main.config;
import static net.mathias2246.buildmc.Main.configFile;

public class EndEventCommand implements CustomCommand {
    @Override
    public CommandAPICommand getCommand() {

        var cmd = new CommandAPICommand("endevent");
        cmd.withPermission("buildmc.operator");

        var allow = new BooleanArgument("allow");
        allow.withPermission("buildmc.operator");

        cmd.executes((command) -> {});

        allow.executes(
                (command) -> {

                    EndListener.allowEnd = Boolean.TRUE.equals(command.args().getByClass("allow", Boolean.class));
                    config.set("end-event.allow-end", EndListener.allowEnd);
                    try {
                        config.save(configFile);
                    } catch (IOException ignored) {
                    }
                }
        );

        cmd.withArguments(allow);

        return cmd;
    }
}
