package net.mathias2246.buildmc.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import org.bukkit.permissions.Permission;

public class BroadcastCommandPlatform implements CustomCommand {
    @Override
    public CommandAPICommand getCommand() {

        return new CommandAPICommand("broadcast")
                .withArguments(new GreedyStringArgument("message"))
                .executes((sender, args) -> {
                    String message = (String) args.get("message");
                    BroadcastCommand.execute(sender, message);
                })
                .withRequirement(sender -> sender.hasPermission(new Permission("buildmc.broadcast")));
    }
}
