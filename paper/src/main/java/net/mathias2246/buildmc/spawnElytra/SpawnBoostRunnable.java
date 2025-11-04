package net.mathias2246.buildmc.spawnElytra;

import net.mathias2246.buildmc.Main;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.GameMode;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import static net.mathias2246.buildmc.Main.*;

public class SpawnBoostRunnable {

    public final int multiplyValue;
    private final boolean boostEnabled;

    public final Player player;

    /**Checks if the given Player uses the spawn elytra.
     * @return True if, the player has the uses_spawn_elytra metadata*/
    public static boolean isUsingSpawnElytra(@NotNull Player player) {
        return player.hasMetadata("uses_spawn_elytra");
    }

    /**Checks if the given Player uses the spawn elytra.
     * @return True if, the player has the uses_spawn_elytra metadata*/
    public static boolean isUsingSpawnElytra(@NotNull Entity entity) {
        return entity.hasMetadata("uses_spawn_elytra");
    }

    /**Checks if the given Player uses the spawn elytra boost.
     * @return True if, the player has the uses_spawn_elytra_boost metadata*/
    public static boolean isPlayerBoosted(@NotNull Player player) {
        return player.hasMetadata("uses_spawn_elytra_boost");
    }

    /**Checks if the given Player uses the spawn elytra boost.
     * @return True if, the player has the uses_spawn_elytra_boost metadata*/
    public static boolean isPlayerBoosted(@NotNull Entity entity) {
        return entity.hasMetadata("uses_spawn_elytra_boost");
    }

    public static void resetBoost(@NotNull Player player) {
        player.removeMetadata("uses_spawn_elytra_boost", plugin);
    }

    public static void resetBoost(@NotNull Entity entity) {
        entity.removeMetadata("uses_spawn_elytra_boost", plugin);
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
        player.removeMetadata("uses_spawn_elytra_boost", plugin);
        player.removeMetadata("uses_spawn_elytra", plugin);
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

        player.setMetadata("uses_spawn_elytra", new FixedMetadataValue(plugin, null));
    }

    public SpawnBoostRunnable(Player player) {
        this.player = player;

        this.multiplyValue = config.getInt("spawn-elytra.strength", 2);
        this.boostEnabled = config.getBoolean("spawn-elytra.enabled", true);
    }

    public static void applyBoost(@NotNull Player player, double multiplier, double verticalVelocity) {
        player.setMetadata("uses_spawn_elytra_boost", new FixedMetadataValue(plugin, null));
        applyRawBoost(player, multiplier, verticalVelocity);
    }

    public static void applyRawBoost(@NotNull Player player, double multiplier, double verticalVelocity) {
        player.setVelocity(player.getLocation().getDirection().multiply(multiplier).setY(verticalVelocity));
    }
}
