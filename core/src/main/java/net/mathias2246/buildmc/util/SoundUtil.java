package net.mathias2246.buildmc.util;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.mathias2246.buildmc.CoreMain;

public final class SoundUtil {

    @SuppressWarnings("PatternValidation")
    public static void setup() {
        notification = Sound.sound(
                Key.key(CoreMain.config.getString("sounds.notification", "minecraft:entity.item.pickup")),
                Sound.Source.UI,
                1f,
                1f
        );
        mistake = Sound.sound(
                Key.key(CoreMain.config.getString("sounds.mistake", "minecraft:block.note_block.snare")),
                Sound.Source.UI,
                1f,
                1f
        );
        success = Sound.sound(
                Key.key(CoreMain.config.getString("sounds.success", "minecraft:block.note_block.bell")),
                Sound.Source.UI,
                1f,
                1f
        );
        uiClick = Sound.sound(
                Key.key(CoreMain.config.getString("sounds.ui.click", "minecraft:ui.button.click")),
                Sound.Source.UI,
                1f,
                1f
        );
        clickHigh = Sound.sound(
                Key.key(CoreMain.config.getString("sounds.ui.click-high", "minecraft:block.copper_bulb.turn_on")),
                Sound.Source.UI,
                1f,
                1f
        );
        clickLow = Sound.sound(
                Key.key(CoreMain.config.getString("sounds.ui.click-low", "minecraft:block.copper_bulb.turn_off")),
                Sound.Source.UI,
                1f,
                1f
        );
    }

    public static Sound notification;
    public static Sound mistake;
    public static Sound success;
    public static Sound uiClick;
    public static Sound clickHigh;
    public static Sound clickLow;
}
