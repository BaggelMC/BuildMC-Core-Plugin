package net.mathias2246.buildmc.claims.listeners;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.claims.ClaimManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

public class ChangeArmorStandListener implements Listener {
    @EventHandler
    public void onPlayerChangeArmorStand(PlayerArmorStandManipulateEvent event) {
        Player player = event.getPlayer();
        if (!ClaimManager.isPlayerAllowed(CoreMain.claimManager, player, event.getRightClicked().getLocation())) {
            CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.armor-stand-manipulate"));
            event.setCancelled(true);
        }
    }
}
