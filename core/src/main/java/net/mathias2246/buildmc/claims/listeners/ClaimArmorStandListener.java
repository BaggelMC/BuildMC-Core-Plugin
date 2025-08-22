package net.mathias2246.buildmc.claims.listeners;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.claims.ProtectionFlag;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

import java.util.EnumSet;

public class ClaimArmorStandListener implements Listener {
    @EventHandler
    public void onArmorStandInteract(PlayerArmorStandManipulateEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        if (!ClaimManager.isPlayerAllowed(player, EnumSet.of(ProtectionFlag.INTERACTION_ARMOR_STAND, ProtectionFlag.PREVENT_INTERACTIONS), entity.getLocation())) {
            CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.armor-stand"));
            event.setCancelled(true);
        }
    }
}
