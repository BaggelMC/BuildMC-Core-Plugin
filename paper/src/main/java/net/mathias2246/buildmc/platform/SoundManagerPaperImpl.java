package net.mathias2246.buildmc.platform;

import net.kyori.adventure.sound.Sound;
import net.mathias2246.buildmc.util.SoundManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SoundManagerPaperImpl implements SoundManager {
    @Override
    public void playSound(@NotNull Player player, @NotNull Sound sound) {
        player.playSound(sound);
    }

    @Override
    public void playSound(@NotNull Location location, @NotNull Sound sound) {
        location.getWorld().playSound(sound, location.getX(), location.getY(), location.getZ());
    }
}
