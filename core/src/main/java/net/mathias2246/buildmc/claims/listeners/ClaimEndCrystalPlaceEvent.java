package net.mathias2246.buildmc.claims.listeners;

import net.mathias2246.buildmc.claims.ClaimManager;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class ClaimEndCrystalPlaceEvent implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEndCrystalPlace(CreatureSpawnEvent event) {
        if (!event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.SPAWNER_EGG)) return;

        if (!(event.getEntity() instanceof EnderCrystal crystal)) return;

        if (ClaimManager.hasOwner(event.getLocation().getChunk())) event.setCancelled(true);
    }
}
