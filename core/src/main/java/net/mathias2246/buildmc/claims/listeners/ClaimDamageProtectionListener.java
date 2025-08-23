package net.mathias2246.buildmc.claims.listeners;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.claims.Claim;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.claims.ProtectionFlag;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;

import java.sql.SQLException;

public class ClaimDamageProtectionListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();
        Entity damager = event.getDamageSource().getCausingEntity();

        if (!(damager instanceof Player attacker)) return;

        Claim claim = null;
        try {
            claim = ClaimManager.getClaim(victim.getLocation());
        } catch (SQLException e) {
            CoreMain.plugin.getLogger().severe("SQL error while getting claim: " + e);
        }

        if (claim == null) return;

        if (claim.getWhitelistedPlayers().contains(attacker.getUniqueId())) return;

        if (claim.hasFlag(ProtectionFlag.EXCLUDE_PLAYERS) && victim instanceof Player player) return;

        if (claim.hasFlag(ProtectionFlag.ENTITY_DAMAGE) && !ClaimManager.isPlayerAllowed(attacker, ProtectionFlag.ENTITY_DAMAGE, claim)) {
            CoreMain.mainClass.sendPlayerActionBar(attacker, Component.translatable("messages.claims.not-accessible.entity-damage"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(VehicleDamageEvent event) {
        Vehicle victim = event.getVehicle();
        Entity damager = event.getAttacker();

        if (!(damager instanceof Player attacker)) return;

        if (!ClaimManager.isPlayerAllowed(attacker, ProtectionFlag.ENTITY_DAMAGE, victim.getLocation())) {
            CoreMain.mainClass.sendPlayerActionBar(attacker, Component.translatable("messages.claims.not-accessible.entity-damage"));
            event.setCancelled(true);
        }
    }
}