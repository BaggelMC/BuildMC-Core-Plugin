package net.mathias2246.buildmc.util;

import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**This interface is used to play sounds to a certain player or at a certain location.
 * <p>This interface should be used when wanting to play sounds platform-independent (like Spigot or Paper).</p>*/
public interface SoundManager {
    /**Plays the given sound to a player. <p><b>Only the given player will hear the sound.</b></p>*/
    void playSound(@NotNull Player player, @NotNull Sound sound);
    /**Plays the given sound at a certain location. <p><b>Every player in the range will hear this sound.</b></p>*/
    void playSound(@NotNull Location location, @NotNull Sound sound);
}
