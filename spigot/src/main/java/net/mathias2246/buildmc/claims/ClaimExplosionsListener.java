package net.mathias2246.buildmc.claims;

import net.mathias2246.buildmc.CoreMain;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class ClaimExplosionsListener implements Listener {
    private final boolean blockDamageEnabled = CoreMain.config.getBoolean("claims.protections.damage.explosion-block-damage");
    private final boolean entityDamageEnabled = CoreMain.config.getBoolean("claims.protections.explosion-entity-damage");

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplosion(EntityExplodeEvent event) {
        if (!blockDamageEnabled) return;
        if (ClaimManager.hasOwner(event.getLocation().getChunk())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplosion(BlockExplodeEvent event) {
        if (!blockDamageEnabled) return;
        if (ClaimManager.hasOwner(event.getExplodedBlockState().getChunk())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplosionDamage (EntityDamageEvent event) {
        if (!entityDamageEnabled) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            if (ClaimManager.getOwnerString(event.getEntity().getLocation()) != null) event.setCancelled(true);
        }
    }
}
