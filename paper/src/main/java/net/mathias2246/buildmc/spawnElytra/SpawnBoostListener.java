package net.mathias2246.buildmc.spawnElytra;

import net.mathias2246.buildmc.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import static net.mathias2246.buildmc.Main.plugin;

public class SpawnBoostListener extends BukkitRunnable implements Listener {

    private final FileConfiguration config;
    private final ElytraZoneManager zoneManager;
    public final int multiplyValue;
    private final boolean boostEnabled;

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

    public SpawnBoostListener(ElytraZoneManager zoneManager) {
        this.zoneManager = zoneManager;

        this.config = plugin.getConfig();
        this.multiplyValue = config.getInt("spawn-elytra.strength", 2);
        this.boostEnabled = config.getBoolean("spawn-elytra.enabled", true);

        this.runTaskTimer(plugin, 0, 3);
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isSurvival(player)) continue;

            if (isUsingSpawnElytra(player) && !player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().isAir()) {
                stopFlying(player);
            } else if (!player.isGliding()) {
                boolean inZone = zoneManager.isInZone(player);
                player.setAllowFlight(inZone);
            }
        }
    }

    // Fixes elytra not closing when changing gamemode
    @EventHandler
    public void onGamemodeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        GameMode oldMode = player.getGameMode();
        GameMode newMode = event.getNewGameMode();

        // Only stop flying if leaving survival/adventure -> something else,
        // and the player was using the spawn elytra
        if (isSurvival(oldMode) && !isSurvival(newMode) && isUsingSpawnElytra(player)) {
            stopFlying(player);
        }
    }

    // Fixes player state not resetting when reconnecting
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!config.getBoolean("spawn-elytra.on-join-elytra-check", true)) return;

        Player player = event.getPlayer();

        stopFlying(player);

        if (isSurvival(player)) {
            player.setAllowFlight(false);
        }

    }

    @EventHandler
    public void onDoubleJump(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (!isSurvival(player)) return;

        if (!zoneManager.isInZone(player)) return;

        event.setCancelled(true);

        if (player.isGliding()) return;

        setPlayerFlying(player);

        if (boostEnabled && !isPlayerBoosted(player)) {
            player.sendActionBar(Message.msg(player, "messages.spawn-elytra.boost-hint"));
        }
    }

    @EventHandler
    public void onSwapItem(PlayerSwapHandItemsEvent event) {
        if (!boostEnabled) return;

        Player player = event.getPlayer();
        if (!isUsingSpawnElytra(player) || isPlayerBoosted(player)) return;

        event.setCancelled(true);

         applyBoost(player, multiplyValue, 1.2);
    }

    public static void applyBoost(@NotNull Player player, int multiplier, double verticalVelocity) {
        player.setMetadata("uses_spawn_elytra_boost", new FixedMetadataValue(plugin, null));
        applyRawBoost(player, multiplier, verticalVelocity);
    }

    public static void applyRawBoost(@NotNull Player player, int multiplier, double verticalVelocity) {
        player.setVelocity(player.getLocation().getDirection().multiply(multiplier).setY(verticalVelocity));
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (isUsingSpawnElytra(player) && (
                event.getCause() == EntityDamageEvent.DamageCause.FALL ||
                        event.getCause() == EntityDamageEvent.DamageCause.FLY_INTO_WALL)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onToggleGlide(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (isUsingSpawnElytra(player)) {
            event.setCancelled(true);
        }
    }



    // Fixes Issue where players are gliding when riding entities that are not on the ground
    @EventHandler
    public void onEntityMount(EntityMountEvent event) {
        Entity entity = event.getEntity();
        if (isUsingSpawnElytra(entity)) {
            if (entity instanceof Player player) stopFlying(player);
        }
    }
}
