package net.mathias2246.buildmc.endEvent;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
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
                    Component senderMessage = Message.msg(command.getSource().getSender(), senderMessageKey);

                    CommandSender sender = command.getSource().getSender();
                    if (sender instanceof Player player) {
                        player.sendMessage(senderMessage);
                    } else if (sender instanceof ConsoleCommandSender) {
                        sender.sendMessage(senderMessage);
                    } else {
                        // Fallback to legacy if no audience
                        sender.sendMessage(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(senderMessage));
                    }

                    String messageKey = allowEnd ? "messages.end-event.broadcast-opened" : "messages.end-event.broadcast-closed";
                    String broadcastFormat = config.getString("broadcast-format",
                            "§6--------------------------------\n\n%message%\n\n§6--------------------------------");

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        Component localizedMessage = Message.msg(player, messageKey);
                        String formatted = broadcastFormat.replace(
                                "%message%",
                                net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(localizedMessage)
                        );
                        Component finalMessage = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(formatted);
                        player.sendMessage(finalMessage);
                    }
                    return 1;
                }
        );
        cmd.requires((command) -> command.getSender().hasPermission("buildmc.admin"));
        return cmd;
    }
}
