package net.mathias2246.buildmc.claims;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;

import static net.mathias2246.buildmc.Main.audiences;
import static net.mathias2246.buildmc.Main.claimManager;

public class ClaimVehicleEnterListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVehicleEnter(VehicleEnterEvent event) {
        Vehicle victim = event.getVehicle();
        Entity entered = event.getEntered();

        if (!(entered instanceof Player player)) return;
        if (!ClaimManager.isPlayerAllowed(claimManager, player, victim.getLocation())) {
            audiences.player(player).sendActionBar(Component.translatable("messages.claims.not-accessible.vehicle"));
            event.setCancelled(true);
        }
    }

}
