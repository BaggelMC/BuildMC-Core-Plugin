package net.mathias2246.buildmc.platform;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.mathias2246.buildmc.util.SoundManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static net.mathias2246.buildmc.Main.audiences;
import static net.mathias2246.buildmc.Main.config;

public class SoundManagerSpigotImpl implements SoundManager {

    @SuppressWarnings("PatternValidation")
    public static void setup() {
        notification = Sound.sound(
                Key.key(config.getString("sounds.notification", "minecraft:entity.item.pickup")),
                Sound.Source.MASTER,
                1f,
                1f
        );
        mistake = Sound.sound(
                Key.key(config.getString("sounds.mistake", "minecraft:block.note_block.snare")),
                Sound.Source.MASTER,
                1f,
                1f
        );
        success = Sound.sound(
                Key.key(config.getString("sounds.success", "minecraft:block.note_block.bell")),
                Sound.Source.MASTER,
                1f,
                1f
        );
    }

    public static Sound notification;
    public static Sound mistake;
    public static Sound success;


    @Override
    public void playSound(@NotNull Player player, @NotNull Sound sound) {
        audiences.player(player).playSound(sound);
    }

    @SuppressWarnings("PatternValidation")
    @Override
    public void playSound(@NotNull Location location, @NotNull Sound sound) {
        audiences.world(Key.key(Objects.requireNonNull(location.getWorld()).getKey().toString())).playSound(sound, location.getX(), location.getY(), location.getZ());
    }
}
