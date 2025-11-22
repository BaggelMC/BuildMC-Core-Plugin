package net.mathias2246.buildmc.spawnElytra;

import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SpawnElytraUtil {
    public static final @NotNull NamespacedKey USES_SPAWN_ELYTRA_KEY = Objects.requireNonNull(NamespacedKey.fromString("buildmc:uses_spawn_elytra"));
    public static final @NotNull NamespacedKey USES_SPAWN_ELYTRA_BOOST_KEY = Objects.requireNonNull(NamespacedKey.fromString("buildmc:uses_spawn_elytra_boost"));


    /**Checks if the given Player uses the spawn elytra.
     * @return True if, the player has the uses_spawn_elytra metadata*/
    public static boolean isUsingSpawnElytra(@NotNull Player player) {
        return player.getPersistentDataContainer().has(USES_SPAWN_ELYTRA_KEY);
    }

    /**Checks if the given Player uses the spawn elytra.
     * @return True if, the player has the uses_spawn_elytra metadata*/
    public static boolean isUsingSpawnElytra(@NotNull Entity entity) {
        return entity.getPersistentDataContainer().has(USES_SPAWN_ELYTRA_KEY);
    }

    /**Checks if the given Player uses the spawn elytra boost.
     * @return True if, the player has the uses_spawn_elytra_boost metadata*/
    public static boolean isPlayerBoosted(@NotNull Player player) {
        return player.getPersistentDataContainer().has(USES_SPAWN_ELYTRA_BOOST_KEY);
    }

    /**Checks if the given Player uses the spawn elytra boost.
     * @return True if, the player has the uses_spawn_elytra_boost metadata*/
    public static boolean isPlayerBoosted(@NotNull Entity entity) {
        return entity.getPersistentDataContainer().has(USES_SPAWN_ELYTRA_BOOST_KEY);
    }

    public static void resetBoost(@NotNull Player player) {
        player.getPersistentDataContainer().remove(USES_SPAWN_ELYTRA_BOOST_KEY);
    }

    public static void resetBoost(@NotNull Entity entity) {
        entity.getPersistentDataContainer().remove(USES_SPAWN_ELYTRA_BOOST_KEY);
    }

    /**Checks if the player is in survival or adventure mode.*/
    public static boolean isSurvival(@NotNull Player player) {
        return player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE;
    }

    /**Checks if the gamemode is survival or adventure mode.*/
    public static boolean isSurvival(@NotNull GameMode gameMode) {
        return gameMode == GameMode.SURVIVAL || gameMode == GameMode.ADVENTURE;
    }

    /**Stops the player from flying.
     * Removes all spawn-elytra related metadata and resets all flight related attributes.*/
    public static void stopFlying(@NotNull Player player) {
        player.getPersistentDataContainer().remove(USES_SPAWN_ELYTRA_BOOST_KEY);
        player.getPersistentDataContainer().remove(USES_SPAWN_ELYTRA_KEY);
        player.setFallDistance(0);
        if (isSurvival(player)) player.setAllowFlight(false);
        player.setFlying(false);
        player.setGliding(false);

    }

    /**Makes the player glide using the spawn-elytra.*/
    public static void setPlayerFlying(@NotNull Player player) {
        var mode = player.getGameMode();
        if (!isSurvival(player)) return;
        player.setGliding(true);
        player.setAllowFlight(false);
        player.setFlying(false);

        player.getPersistentDataContainer().set(USES_SPAWN_ELYTRA_KEY, PersistentDataType.BOOLEAN, true);
    }

    public static void applyBoost(@NotNull Player player, double multiplier, double verticalVelocity) {
        player.getPersistentDataContainer().set(USES_SPAWN_ELYTRA_BOOST_KEY, PersistentDataType.BOOLEAN, true);
        applyRawBoost(player, multiplier, verticalVelocity);
    }

    public static void applyRawBoost(@NotNull Player player, double multiplier, double verticalVelocity) {
        player.setVelocity(player.getLocation().getDirection().multiply(multiplier).setY(verticalVelocity));
    }
}
