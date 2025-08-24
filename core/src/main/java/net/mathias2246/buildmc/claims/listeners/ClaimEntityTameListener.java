package net.mathias2246.buildmc.claims.listeners;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.claims.ProtectionFlag;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTameEvent;

import java.util.EnumSet;

public class ClaimEntityTameListener implements Listener {
    @EventHandler
    public void onTame(EntityTameEvent event) {
        if (!(event.getOwner() instanceof Player player)) return;
        Entity entity = event.getEntity();

        if (!ClaimManager.isPlayerAllowed(player, EnumSet.of(ProtectionFlag.INTERACTION_TAME_ENTITY), entity.getLocation())) {
            CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.tame-entity"));
            event.setCancelled(true);
        }
    }
}
