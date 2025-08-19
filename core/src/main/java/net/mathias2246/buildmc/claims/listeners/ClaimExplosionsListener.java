package net.mathias2246.buildmc.claims.listeners;

import net.mathias2246.buildmc.claims.ClaimManager;
import org.bukkit.Location;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class ClaimExplosionsListener implements Listener {

    private boolean windCharges = true;

    public ClaimExplosionsListener(boolean allowWindCharges) {
        windCharges = allowWindCharges;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplosion(EntityExplodeEvent event) {
        Location location = event.getLocation();

        // Dont suppress wind charge boosts
        if (windCharges && event.getEntity() instanceof WindCharge) {
            if (ClaimManager.hasOwner(location.getChunk())) event.blockList().clear();
            return;
        }

        if (!ClaimManager.hasOwner(location.getChunk())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplosion(BlockExplodeEvent event) {
        Location location = event.getBlock().getLocation();

        if (!ClaimManager.hasOwner(location.getChunk())) return;
        event.setCancelled(true);
    }
}
