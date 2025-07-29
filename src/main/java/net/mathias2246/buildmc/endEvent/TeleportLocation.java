package net.mathias2246.buildmc.endEvent;

import net.mathias2246.buildmc.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

public enum TeleportLocation {
    SPAWNPOINT,
    WORLDSPAWN;

    /**Return where players are teleported when they enter the end portal while the end is disabled.
     * @return The value of the string, or SPAWNPOINT as fallback*/
    public static @NotNull TeleportLocation fromString(@Nullable String string) {

        if (string == null) return SPAWNPOINT;

        string = string.toUpperCase();
        try {
            return valueOf(string);
        } catch (IllegalArgumentException e) {
            return SPAWNPOINT;
        }

    }
}
