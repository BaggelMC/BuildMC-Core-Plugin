package net.mathias2246.buildmc.util;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.mathias2246.buildmc.CoreMain;

public class SoundUtil {

    @SuppressWarnings("PatternValidation")
    public static void setup() {
        notification = Sound.sound(
                Key.key(CoreMain.config.getString("sounds.notification", "minecraft:entity.item.pickup")),
                Sound.Source.MASTER,
                1f,
                1f
        );
        mistake = Sound.sound(
                Key.key(CoreMain.config.getString("sounds.mistake", "minecraft:block.note_block.snare")),
                Sound.Source.MASTER,
                1f,
                1f
        );
        success = Sound.sound(
                Key.key(CoreMain.config.getString("sounds.success", "minecraft:block.note_block.bell")),
                Sound.Source.MASTER,
                1f,
                1f
        );
    }

    public static Sound notification;
    public static Sound mistake;
    public static Sound success;

}
