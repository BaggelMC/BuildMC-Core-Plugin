package net.mathias2246.buildmc.commands;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.util.AudienceUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class CommandUtil {
    public static @Nullable Player requiresPlayer(CommandSender sender) {
        if (sender instanceof Player player) {
            return player;
        } else {
             AudienceUtil.sendMessage(sender, Component.translatable("messages.error.not-a-player"));
            return null;
        }
    }
}
