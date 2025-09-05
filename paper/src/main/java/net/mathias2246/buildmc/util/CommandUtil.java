package net.mathias2246.buildmc.util;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public final class CommandUtil {

    public static @Nullable Player requiresPlayer(CommandContext<CommandSourceStack> command) {
        CommandSender source = command.getSource().getSender();
        if (source instanceof Player player) {
            return player;
        } else {
            source.sendMessage(Component.translatable("messages.error.not-a-player"));
            return null;
        }
    }

}
