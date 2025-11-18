package net.mathias2246.buildmc.commands;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.util.SoundUtil;
import org.bukkit.command.CommandSender;

public class BroadcastCommand {
    private static final Component separator = Component.text("==============================", NamedTextColor.GRAY);

    public static int execute(CommandSender sender, String input) {

        MiniMessage mm = MiniMessage.miniMessage();

        Component component;

        try {
            component = mm.deserialize(input);
        } catch (Exception e) {
            CoreMain.mainClass.sendMessage(sender, Component.text(
                    "Failed to parse MiniMessage: " + e.getMessage(),
                    NamedTextColor.RED
            ));

            return 0;
        }

        Audience audience = CoreMain.bukkitAudiences.players();

        // If you can find a better solution go ahead, but I can't be fucked to deal with the newlines rn
        audience.sendMessage(Component.empty());
        audience.sendMessage(separator);
        audience.sendMessage(Component.empty());
        audience.sendMessage(component);
        audience.sendMessage(Component.empty());
        audience.sendMessage(separator);
        audience.sendMessage(Component.empty());

        audience.playSound(SoundUtil.notification);

        CoreMain.mainClass.sendMessage(sender, Component.text(
                "Broadcast sent!",
                NamedTextColor.GREEN
        ));

        return 1;
    }

}
