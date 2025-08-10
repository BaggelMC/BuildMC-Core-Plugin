package net.mathias2246.buildmc.claims.listeners;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.claims.ClaimManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class ClaimNameTagUseListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onNameTagUse(PlayerInteractEntityEvent event) {
        if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.NAME_TAG) return;

        Player player = event.getPlayer();
        if (!ClaimManager.isPlayerAllowed(CoreMain.claimManager, player, event.getRightClicked().getLocation())) {
            event.setCancelled(true);
            CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.entity-rename"));
        }
    }
}
