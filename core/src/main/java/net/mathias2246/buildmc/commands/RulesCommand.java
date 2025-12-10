package net.mathias2246.buildmc.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mathias2246.buildmc.CoreMain;
import org.bukkit.command.CommandSender;

public class RulesCommand {
    private static final Component separator = Component.text("==============================", NamedTextColor.GRAY);

    public static int execute(CommandSender sender) {
        CoreMain.plugin.sendMessage(sender, Component.translatable("messages.rules"));
        return 1;
    }

}
