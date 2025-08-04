package net.mathias2246.buildmc.claims;

import net.mathias2246.buildmc.Main;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class ClaimEntityChangeBlockListener implements Listener {

    private final boolean preventWither = Main.config.getBoolean("claims.protections.entity-modifications.wither");
    private final boolean preventEnderman = Main.config.getBoolean("claims.protections.entity-modifications.enderman");
    private final boolean preventRavager = Main.config.getBoolean("claims.protections.entity-modifications.ravager");

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        EntityType entityType = event.getEntityType();
        Location location = event.getBlock().getLocation();

        if (ClaimManager.getClaimTeam(location) == null) return;

        // Apply protections
        if (preventWither && entityType == EntityType.WITHER) {
            event.setCancelled(true);
        } else if (preventEnderman && entityType == EntityType.ENDERMAN) {
            event.setCancelled(true);
        } else if (preventRavager && entityType == EntityType.RAVAGER) {
            event.setCancelled(true);
        }
    }
}
