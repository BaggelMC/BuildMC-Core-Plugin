package net.mathias2246.buildmc.commands;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.mathias2246.buildmc.CoreMain;
import org.bukkit.command.CommandSender;

public class BroadcastCommand {
    private static final Component separator = Component.text("==============================", NamedTextColor.GRAY);

    @SuppressWarnings("PatternValidation")
    private static final Sound notificationSound = Sound.sound(
            Key.key(CoreMain.plugin.getConfig().getString("sounds.notification", "minecraft:entity.item.pickup")),
            Sound.Source.MASTER,
            1f,
            1f

    );

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

        audience.playSound(notificationSound);

        CoreMain.mainClass.sendMessage(sender, Component.text(
                "Broadcast sent!",
                NamedTextColor.GREEN
        ));

        return 1;
    }

}
