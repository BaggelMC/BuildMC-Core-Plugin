package net.mathias2246.buildmc.util;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

import static net.mathias2246.buildmc.CoreMain.plugin;

public class CommandUtils {
    public static @Nullable CommandMap getCommandMap() {
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            return (CommandMap) commandMapField.get(Bukkit.getServer());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to retrieve the command map.");
            return null;
        }
    }
}
