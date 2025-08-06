package net.mathias2246.buildmc.claims.listeners;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.claims.ProtectionFlag;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;

import java.util.EnumSet;

public class ClaimVehicleEnterListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVehicleEnter(VehicleEnterEvent event) {
        Vehicle victim = event.getVehicle();
        Entity entered = event.getEntered();

        if (!(entered instanceof Player player)) return;
        if (!ClaimManager.isPlayerAllowed(player, EnumSet.of(ProtectionFlag.VEHICLE_ENTER), victim.getLocation())) {
            CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.vehicle"));
            event.setCancelled(true);
        }
    }
}
