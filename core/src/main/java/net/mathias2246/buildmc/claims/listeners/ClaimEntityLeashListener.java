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
import org.bukkit.event.entity.PlayerLeashEntityEvent;

import java.util.EnumSet;

public class ClaimEntityLeashListener implements Listener {
    // FIXME: Does not work
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityLeash(PlayerLeashEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getEntity();

        if (ClaimManager.isPlayerAllowed(player, EnumSet.of(ProtectionFlag.INTERACTION_ATTACH_LEASH, ProtectionFlag.PREVENT_INTERACTIONS), entity.getLocation())) {
            CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.attach-leash"));
            event.setCancelled(true);
        }
    }
}
