package net.mathias2246.buildmc.endEvent;

import dev.jorel.commandapi.CommandAPICommand;
import net.mathias2246.buildmc.commands.CustomCommand;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;

import java.io.IOException;

import static net.mathias2246.buildmc.Main.config;
import static net.mathias2246.buildmc.Main.configFile;

public class EndEventCommand implements CustomCommand {

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

                    String messageKey = allowEnd ? "end-event.broadcast-opened" : "end-event.broadcast-closed";
                    String rawMessage = config.getString(messageKey,
                            allowEnd ? "§eThe End has been §aOPENED§e!" : "§eThe End has been §cCLOSED§e!");

                    BaseComponent broadcast = TextComponent.fromLegacy(rawMessage);
                    Bukkit.spigot().broadcast(broadcast);

                    command.sender().sendMessage("§aSet 'allow end' to: '" + (allowEnd ? "open" : "close") + "'.");
                });
    }
}
