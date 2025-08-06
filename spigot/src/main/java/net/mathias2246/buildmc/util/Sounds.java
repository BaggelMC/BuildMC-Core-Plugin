package net.mathias2246.buildmc.util;


import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"UnsubstitutedExpression", "PatternValidation"})
public class Sounds {

    public static Sound MISTAKE;
    public static Sound SUCCESS;

    public static void setup() {

        MISTAKE = Sound.sound(
            Key.key(config.getString("sounds.mistake", "minecraft:block.note_block.snare")),
                Sound.Source.MASTER,
                1f,
                1f

        );

        SUCCESS = Sound.sound(
                Key.key(config.getString("sounds.success", "minecraft:block.note_block.bell")),
                Sound.Source.MASTER,
                1f,
                1f

        );
    }

    public static void playSound(@NotNull Player player, @NotNull Sound sound) {
        audiences.player(player).playSound(sound);
    }

}
