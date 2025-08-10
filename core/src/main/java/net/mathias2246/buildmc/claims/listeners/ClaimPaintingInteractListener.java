package net.mathias2246.buildmc.claims.listeners;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.claims.ClaimManager;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;

public class ClaimPaintingInteractListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPaintingBreak(HangingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player player)) return;
        if (!(event.getEntity() instanceof Painting)) return;

        if (!ClaimManager.isPlayerAllowed(CoreMain.claimManager, player, event.getEntity().getLocation())) {
            event.setCancelled(true);
            CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.entity-damage"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPaintingPlace(HangingPlaceEvent event) {
        Player player = event.getPlayer();

        if (player == null) return;

        if (!ClaimManager.isPlayerAllowed(CoreMain.claimManager, player, event.getEntity().getLocation())) {
            event.setCancelled(true);
            CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.block-place"));
        }
    }
}
