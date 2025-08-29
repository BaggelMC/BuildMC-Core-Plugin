package net.mathias2246.buildmc.util;

import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Provides a platform-independent way to play sounds in BuildMC.
 * <p>
 * This interface abstracts away differences between server implementations
 * (Paper vs Spigot) so that plugins and addons can play sounds without
 * worrying about platform-specific APIs.
 * </p>
 */
public interface SoundManager {

    /**
     * Plays a sound to a specific player.
     * <p>
     * <b>Only the given player will hear the sound.</b>
     * </p>
     *
     * @param player the player to hear the sound
     * @param sound  the {@link Sound} to play
     */
    void playSound(@NotNull Player player, @NotNull Sound sound);

    /**
     * Plays a sound at a specific location in the world.
     * <p>
     * <b>All players within audible range of the location will hear the sound.</b>
     * </p>
     *
     * @param location the {@link Location} where the sound should be played
     * @param sound    the {@link Sound} to play
     */
    void playSound(@NotNull Location location, @NotNull Sound sound);
}
