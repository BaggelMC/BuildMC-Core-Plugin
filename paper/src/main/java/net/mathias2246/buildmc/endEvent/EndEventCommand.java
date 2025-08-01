package net.mathias2246.buildmc.endEvent;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;

import static net.mathias2246.buildmc.Main.config;
import static net.mathias2246.buildmc.Main.configFile;

public class EndEventCommand {

    public LiteralArgumentBuilder<CommandSourceStack> getCommandBuilder() {
        var cmd = Commands.literal("endevent");
        cmd.requires(
                (command) -> command.getSender().hasPermission("buildmc.admin")
        );

        cmd.then(getSubCommand("open", true));
        cmd.then(getSubCommand("close", false));

        return cmd;

    }

    private LiteralArgumentBuilder<CommandSourceStack> getSubCommand(String name, boolean allowEnd) {
        var cmd = Commands.literal(name);
        cmd.executes(
                (command) -> {
                    EndListener.allowEnd = allowEnd;
                    config.set("end-event.allow-end", allowEnd);
                    try {
                        config.save(configFile);
                    } catch (IOException ignored) {
                    }

                    String senderMessageKey = allowEnd ? "messages.end-event.opened" : "messages.end-event.closed";
                    Component senderMessage = Component.translatable(senderMessageKey);
                    command.getSource().getSender().sendMessage(senderMessage);

                    String messageKey = allowEnd ? "messages.end-event.broadcast-opened" : "messages.end-event.broadcast-closed";
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        Component msg = Component.translatable(messageKey);
                        player.sendMessage(msg);
                    }
                    return 1;
                }
        );
        cmd.requires((command) -> command.getSender().hasPermission("buildmc.admin"));
        return cmd;
    }
}
