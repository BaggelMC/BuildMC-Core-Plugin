package net.mathias2246.buildmc.claims.listeners;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.claims.ClaimManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTameEvent;

import static net.mathias2246.buildmc.CoreMain.claimManager;
import static net.mathias2246.buildmc.CoreMain.mainClass;

public class ClaimEntityTameListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityTamed(EntityTameEvent event) {
        Entity entity = event.getEntity();
        if (!ClaimManager.hasOwner(entity.getLocation().getChunk())) return;

        Player player = Bukkit.getPlayer(event.getOwner().getUniqueId());
        if (player == null) return;

        if (!ClaimManager.isPlayerAllowed(claimManager, player, entity.getLocation())) {
            mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.entity-tame"));
            event.setCancelled(true);
        }
    }
}
