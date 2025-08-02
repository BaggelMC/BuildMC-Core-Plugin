package net.mathias2246.buildmc.status;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.commands.CustomCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static com.mojang.brigadier.arguments.StringArgumentType.string;

public record SetStatusCommand(@NotNull StatusConfig config) implements CustomCommand {

    // The '/status' command
    // This command has two sub commands
    //  - 'remove': Removes your current status
    //  - 'set': Sets your current status
    @Override
    public LiteralCommandNode<CommandSourceStack> getCommand() {
        var cmd = Commands.literal("status");
        cmd.executes(
                (command) -> {
                    command.getSource().getSender().sendMessage("/status <args>");
                    return 1;
                }
        );


        var removeSub = Commands.literal("remove");
        removeSub.executes(
                (command) -> {
                    if (!(command.getSource().getSender() instanceof Player player)) {
                        command.getSource().getSender().sendMessage(Component.translatable("messages.status.only-players"));
                        return 0;
                    }
                    PlayerStatus.removePlayerStatus(player);
                    player.sendMessage(Component.translatable( "messages.status.successfully-removed"));
                    return 1;
                }
        );

        var setSub = Commands.literal("set")
            .executes(
                    (command) -> {
                        if (!(command.getSource().getSender() instanceof Player player)) {
                            command.getSource().getSender().sendMessage(Component.translatable("messages.status.only-players"));
                            return 0;
                        }
                        return 1;
                    }
            );

        var setSubArg = Commands.argument("status_id", string())
                .executes(
                        (command) -> {
                            if (!(command.getSource().getSender() instanceof Player player)) {
                                command.getSource().getSender().sendMessage(Component.translatable("messages.status.only-players"));
                                return 0;
                            }
                            PlayerStatus.setPlayerStatus(player, command.getArgument("status_id", String.class), false);
                            return 1;
                        }
                );

        setSubArg.suggests(
                (ctx, builder) -> {

                    for (var statusId : StatusConfig.loadedStatuses.keySet()) {
                        builder.suggest(statusId);
                    }

                    return builder.buildFuture();
                }
        );

        setSub.then(setSubArg);

        cmd.then(removeSub);
        cmd.then(setSub);

        return cmd.build();
    }
}
