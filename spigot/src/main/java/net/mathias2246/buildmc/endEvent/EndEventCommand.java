package net.mathias2246.buildmc.endEvent;

import dev.jorel.commandapi.CommandAPICommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.mathias2246.buildmc.commands.CustomCommand;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

import static net.mathias2246.buildmc.Main.config;
import static net.mathias2246.buildmc.Main.configFile;

public class EndEventCommand implements CustomCommand {

    private final BukkitAudiences audiences;

    public EndEventCommand(BukkitAudiences audiences) {
        this.audiences = audiences;
    }

    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("endevent")
                .withPermission("buildmc.operator")
                .withSubcommand(getSubCommand("open", true))
                .withSubcommand(getSubCommand("close", false));
    }

    private CommandAPICommand getSubCommand(String name, boolean allowEnd) {
        return new CommandAPICommand(name)
                .executes((command) -> {
                    EndListener.allowEnd = allowEnd;
                    config.set("end-event.allow-end", allowEnd);
                    try {
                        config.save(configFile);
                    } catch (IOException ignored) {
                    }

                    String senderMessageKey = allowEnd ? "messages.end-event.opened" : "messages.end-event.closed";
                    Component senderMessage = Message.msg(command.sender(), senderMessageKey);

                    CommandSender sender = command.sender();
                    if (sender instanceof Player player) {
                        audiences.player(player).sendMessage(senderMessage);
                    } else if (sender instanceof ConsoleCommandSender) {
                        audiences.console().sendMessage(senderMessage);
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
                        audiences.player(player).sendMessage(finalMessage);
                    }
                });
    }
}
