package net.mathias2246.buildmc.spawnElytra;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import static net.mathias2246.buildmc.CoreMain.plugin;
import static net.mathias2246.buildmc.spawnElytra.SpawnElytraUtil.*;

public class SpawnBoostListener extends BukkitRunnable implements Listener {

    private final FileConfiguration config;
    private final ElytraZoneManager zoneManager;

    public SpawnBoostListener(ElytraZoneManager zoneManager) {
        this.zoneManager = zoneManager;

        this.config = plugin.getConfig();

        this.runTaskTimer(plugin, 0, 3);
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isSurvival(player)) continue;

            Location blockCheck = player.getLocation();
            blockCheck.setY(blockCheck.getY()-0.2);

            Material blockType = blockCheck.getBlock().getType();
            if (isUsingSpawnElytra(player) && !BLOCK_EXCEPTION_LOOKUP [blockType.ordinal()]) {
                stopFlying(player);
            } else if (!player.isGliding()) {
                boolean inZone = zoneManager.isInZone(player);
                player.setAllowFlight(inZone);
            }
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
}
