package net.mathias2246.buildmc.player.status;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.StringArgument;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.commands.CustomCommand;
import net.mathias2246.buildmc.status.StatusConfig;
import org.bukkit.entity.Player;
import org.bukkit.help.HelpTopic;
import org.jetbrains.annotations.NotNull;

import static net.mathias2246.buildmc.commands.CommandUtil.requiresPlayer;

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
                    if (!(requiresPlayer(command.sender()) instanceof Player player)) return;
                    CoreMain.statusManager.removePlayerStatus(player);
                }
        );

        var setSub = new CommandAPICommand("set")
            .executes(
                    (command) -> {
                        if (!(requiresPlayer(command.sender()) instanceof Player player)) return;
                        CoreMain.statusManager.setPlayerStatus(player, command.args().getByClass("status", String.class), false);
                    }
            )
                .withArguments(
                        new StringArgument("status")
                                .replaceSuggestions(
                                        (info, builder) -> {
                                            String remaining = builder.getRemaining().toLowerCase();

                                            for (var ke : CoreMain.statusesRegistry.keySet()) {
                                                var k = ke.toString().replace("buildmc:", "");
                                                if (k.toLowerCase().startsWith(remaining)) {
                                                    builder.suggest(k);
                                                }
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
