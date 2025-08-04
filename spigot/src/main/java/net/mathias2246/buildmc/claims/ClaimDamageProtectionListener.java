package net.mathias2246.buildmc.claims;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.Main;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;

import static net.mathias2246.buildmc.Main.audiences;
import static net.mathias2246.buildmc.Main.claimManager;

public class ClaimDamageProtectionListener implements Listener {
    public final boolean excludePlayers = Main.config.getBoolean("claims.protections.damage.exclude-players");

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();
        Entity damager = event.getDamageSource().getCausingEntity();

        if (!(damager instanceof Player attacker)) return;

        // Skip player-on-player protection if excludePlayers is true
        if (excludePlayers && victim instanceof Player player) return;

        // Check if the attacker is allowed to deal damage in the victim's location
        if (!ClaimManager.isPlayerAllowed(claimManager, attacker, victim.getLocation())) {
            audiences.player(attacker).sendActionBar(Component.translatable("messages.claims.not-accessible.entity-damage"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(VehicleDamageEvent event) {
        Vehicle victim = event.getVehicle();
        Entity damager = event.getAttacker();

        if (!(damager instanceof Player attacker)) return;
        if (!ClaimManager.isPlayerAllowed(claimManager, attacker, victim.getLocation())) {
            audiences.player(attacker).sendActionBar(Component.translatable("messages.claims.not-accessible.entity-damage"));
            event.setCancelled(true);
        }
    }

}
