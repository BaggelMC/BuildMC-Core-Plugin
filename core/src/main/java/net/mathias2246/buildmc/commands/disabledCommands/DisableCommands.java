package net.mathias2246.buildmc.commands.disabledCommands;

import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.util.CommandUtils;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Map;

import static net.mathias2246.buildmc.CoreMain.plugin;

public class DisableCommands {

    @ApiStatus.Internal
    public static void disableCommand(String namespace, String commandName) {
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = CommandUtils.getCommandMap();

            if (!(commandMap instanceof SimpleCommandMap)) {
                plugin.getLogger().warning("Unsupported CommandMap implementation. Cannot disable command: " + commandName);
                return;
            }

            // Cast to access knownCommands
            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);

            // Remove all relevant keys
            knownCommands.keySet().removeIf(key ->
                    key.equalsIgnoreCase(commandName) ||
                            key.equalsIgnoreCase(namespace + ":" + commandName) ||
                            key.endsWith(":" + commandName));

            // Register dummy override
            Command blockedCommand = new Command(commandName) {
                @Override
                public boolean execute(@NotNull CommandSender sender, @NotNull String label, String[] args) {
                    CoreMain.mainClass.sendMessage(sender, Message.msg(sender, "messages.error.command-disabled"));
                    return true;
                }
            };

            commandMap.register(namespace, blockedCommand);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            plugin.getLogger().warning("Failed to fully disable command '" + commandName + "': " + e.getMessage());
        }
    }

}
