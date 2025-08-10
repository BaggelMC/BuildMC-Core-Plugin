package net.mathias2246.buildmc.claims.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;


public record ClaimEntityChangeBlockListener(boolean blockWither, boolean blockEnderMan, boolean blockRavager) implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        EntityType entityType = event.getEntityType();

        if (blockWither && entityType == EntityType.WITHER) {
            event.setCancelled(true);
        } else if (blockEnderMan && entityType == EntityType.ENDERMAN) {
            event.setCancelled(true);
        } else if (blockRavager && entityType == EntityType.RAVAGER) {
            event.setCancelled(true);
        }
    }
}
