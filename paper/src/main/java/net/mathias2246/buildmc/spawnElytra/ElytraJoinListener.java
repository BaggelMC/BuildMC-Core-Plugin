package net.mathias2246.buildmc.spawnElytra;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import static net.mathias2246.buildmc.Main.*;
import static net.mathias2246.buildmc.spawnElytra.SpawnElytraUtil.*;

public record ElytraJoinListener(boolean boostEnabled, double multiplyValue) implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {


        Player player = event.getPlayer();

        startElytraStateTask(player);

        if (!config.getBoolean("spawn-elytra.on-join-elytra-check", true)) return;

        stopFlying(player);

        if (isSurvival(player)) {
            player.setAllowFlight(false);
        }
    }

    // Reattach runnable to new Player object
    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        startElytraStateTask(player);

        stopFlying(player);

        if (isSurvival(player)) {
            player.setAllowFlight(false);
        }
    }

    private void startElytraStateTask(Player player) {
        player.getScheduler().runAtFixedRate(
                plugin,
                (task) -> {
                    if (!isSurvival(player)) return;

                    Location blockCheck = player.getLocation();
                    blockCheck.setY(blockCheck.getY()-0.2);

                    Material blockType = blockCheck.getBlock().getType();
                    if (isUsingSpawnElytra(player) && !BLOCK_EXCEPTION_LOOKUP [blockType.ordinal()]) {
                        stopFlying(player);
                    } else if (!player.isGliding()) {

                        boolean inZone = zoneManager.isInZone(player);
                        player.setAllowFlight(inZone);
                    }
                },
                null,
                1,
                3
        );
    }

}
