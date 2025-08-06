package net.mathias2246.buildmc.endEvent;

import dev.jorel.commandapi.CommandAPICommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.mathias2246.buildmc.commands.CustomCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;

import static net.mathias2246.buildmc.CoreMain.config;
import static net.mathias2246.buildmc.CoreMain.configFile;

public class EndEventCommand implements CustomCommand {

    private final BukkitAudiences audiences;

    public EndEventCommand(BukkitAudiences audiences) {
        this.audiences = audiences;
    }

    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("endevent")
                .withPermission("buildmc.admin")
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
                    Component senderMessage = Component.translatable(senderMessageKey);
                    audiences.sender(command.sender()).sendMessage(senderMessage);

                    String messageKey = allowEnd ? "messages.end-event.broadcast-opened" : "messages.end-event.broadcast-closed";
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        Component msg = Component.translatable(messageKey);
                        audiences.player(player).sendMessage(msg);
                    }
                });
    }
}
