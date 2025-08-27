package net.mathias2246.buildmc.claims.listeners;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.claims.ProtectionFlag;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

public class ClaimFishingListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProjectileHit(PlayerFishEvent event) {
        Entity entity = event.getCaught();
        Player player = event.getPlayer();

        if (entity == null) return;

        if (!ClaimManager.isPlayerAllowed(player, ProtectionFlag.FISHING, entity.getLocation())) {
            event.setCancelled(true);
            CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.fishing"));
        }

    }

}
