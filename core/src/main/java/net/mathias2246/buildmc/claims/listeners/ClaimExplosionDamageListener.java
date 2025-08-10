package net.mathias2246.buildmc.claims.listeners;

import net.mathias2246.buildmc.claims.ClaimManager;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class ClaimExplosionDamageListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplosionDamage (EntityDamageEvent event) {
        Location location = event.getEntity().getLocation();

        if (ClaimManager.hasOwner(location.getChunk())) {
            if (event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                event.setCancelled(true);
            }
        }
    }
}
