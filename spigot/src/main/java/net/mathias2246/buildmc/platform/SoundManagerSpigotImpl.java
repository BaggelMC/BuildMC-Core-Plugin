package net.mathias2246.buildmc.platform;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.mathias2246.buildmc.util.SoundManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static net.mathias2246.buildmc.Main.audiences;

public class SoundManagerSpigotImpl implements SoundManager {

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
