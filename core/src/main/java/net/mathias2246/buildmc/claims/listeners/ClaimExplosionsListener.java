package net.mathias2246.buildmc.claims.listeners;

import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.claims.Claim;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.claims.ProtectionFlag;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.sql.SQLException;

public class ClaimExplosionsListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplosion(EntityExplodeEvent event) {
        Location location = event.getLocation();

        Claim claim;
        try {
            claim = ClaimManager.getClaim(location);
        } catch (SQLException e) {
            CoreMain.plugin.getLogger().severe("SQL error while getting claim: " + e);
            return;
        }
        if (claim == null) return;

        if (claim.hasFlag(ProtectionFlag.EXPLOSION_BLOCK_DAMAGE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplosion(BlockExplodeEvent event) {
        Location location = event.getBlock().getLocation();

        Claim claim = null;
        try {
            claim = ClaimManager.getClaim(location);
        } catch (SQLException e) {
            CoreMain.plugin.getLogger().severe("SQL error while getting claim: " + e);
        }
        if (claim == null) return;

        if (claim.hasFlag(ProtectionFlag.EXPLOSION_BLOCK_DAMAGE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplosionDamage (EntityDamageEvent event) {
        Location location = event.getEntity().getLocation();

        Claim claim = null;
        try {
            claim = ClaimManager.getClaim(location);
        } catch (SQLException e) {
            CoreMain.plugin.getLogger().severe("SQL error while getting claim: " + e);
        }
        if (claim == null) return;

        if (claim.hasFlag(ProtectionFlag.EXPLOSION_ENTITY_DAMAGE)) {
            if (event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                event.setCancelled(true);
            }
        }
    }
}
