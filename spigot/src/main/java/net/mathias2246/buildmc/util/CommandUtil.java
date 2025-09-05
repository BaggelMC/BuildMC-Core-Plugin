package net.mathias2246.buildmc.util;

import dev.jorel.commandapi.commandsenders.BukkitCommandSender;
import dev.jorel.commandapi.executors.ExecutionInfo;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import static net.mathias2246.buildmc.Main.audiences;

public final class CommandUtil {

    public static @Nullable Player requiresPlayer(ExecutionInfo<CommandSender, BukkitCommandSender<? extends CommandSender>> command) {
        CommandSender source = command.sender();
        if (source instanceof Player player) {
            return player;
        } else {
            audiences.sender(source).sendMessage(Component.translatable("messages.error.not-a-player"));
            return null;
        }
    }

}
