package net.mathias2246.buildmc.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.util.SoundUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RulesCommand {
    private static final Component separator = Component.text("==============================", NamedTextColor.GRAY);

    public static int execute(CommandSender sender) {
        CoreMain.plugin.sendMessage(sender, Component.translatable("messages.rules"));
        if (sender instanceof Player player) CoreMain.soundManager.playSound(player, SoundUtil.notification);
        return 1;
    }

}
