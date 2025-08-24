package net.mathias2246.buildmc.claims.listeners;

import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.claims.Claim;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.claims.ProtectionFlag;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import java.sql.SQLException;

public class ClaimEntityChangeBlockListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        EntityType entityType = event.getEntityType();
        Location location = event.getBlock().getLocation();

        Claim claim;
        try {
            claim = ClaimManager.getClaim(location);
        } catch (SQLException e) {
            CoreMain.plugin.getLogger().severe("SQL error while getting claim: " + e);
            return;
        }

        if (claim == null) return;

        if (claim.hasFlag(ProtectionFlag.ENTITY_MODIFICATIONS_WITHER) && entityType == EntityType.WITHER) {
            event.setCancelled(true);
        } else if (claim.hasFlag(ProtectionFlag.ENTITY_MODIFICATIONS_ENDERMAN) && entityType == EntityType.ENDERMAN) {
            event.setCancelled(true);
        } else if (claim.hasFlag(ProtectionFlag.ENTITY_MODIFICATIONS_RAVAGER) && entityType == EntityType.RAVAGER) {
            event.setCancelled(true);
        }
    }
}
