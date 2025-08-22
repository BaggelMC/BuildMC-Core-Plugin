package net.mathias2246.buildmc.status;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.commands.CustomCommand;
import org.bukkit.entity.Player;
import org.bukkit.help.HelpTopic;
import org.jetbrains.annotations.NotNull;

import static net.mathias2246.buildmc.Main.audiences;

public record SetStatusCommand(@NotNull StatusConfig config) implements CustomCommand {

    // The '/status' command
    // This command has two sub commands
    //  - 'remove': Removes your current status
    //  - 'set': Sets your current status
    @Override
    public CommandAPICommand getCommand() {
        var cmd = new CommandAPICommand("status");
        cmd.executes(
                (command) -> {
                }
        );
        HelpTopic help = new StatusCommandHelp();
        cmd.withHelp(
                help
        );

        cmd.withUsage(
                "remove",
                "set <status>"
        );


        var removeSub = new CommandAPICommand("remove");
        removeSub.executes(
                (command) -> {
                    if (!(command.sender() instanceof Player player)) {
                        audiences.sender(command.sender()).sendMessage(Component.translatable("messages.status.only-players"));
                        return;
                    }
                    PlayerStatus.removePlayerStatus(player);
                }
        );

        var setSub = new CommandAPICommand("set")
            .executes(
                    (command) -> {
                        if (!(command.sender() instanceof Player player)) {
                            audiences.sender(command.sender()).sendMessage(Component.translatable("messages.status.only-players"));
                            return;
                        }
                        PlayerStatus.setPlayerStatus(player, command.args().getByClass("status", String.class), false);
                    }
            )
                .withArguments(
                        new StringArgument("status")
                                .replaceSuggestions(
                                        (info, builder) -> {
                                            for (var k : StatusConfig.loadedStatuses.keySet()) {

                                                builder.suggest(k);
                                            }
                                            return builder.buildFuture();
                                        }
                                )
                )
            .withUsage("set <status>");
        cmd.withSubcommand(
                removeSub
        );
        cmd.withSubcommand(
                setSub
        );


        return cmd;
    }
}
