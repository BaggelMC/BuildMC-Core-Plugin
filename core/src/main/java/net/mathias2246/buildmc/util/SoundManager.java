package net.mathias2246.buildmc.util;

import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface SoundManager {
    void playSound(@NotNull Player player, @NotNull Sound sound);

    void playSound(@NotNull Location location, @NotNull Sound sound);
}
