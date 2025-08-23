package net.mathias2246.buildmc.claims.listeners;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.claims.ProtectionFlag;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class ClaimBonemealInteractListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBonemealUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (event.getItem() == null || event.getItem().getType() != Material.BONE_MEAL) return;

        Player player = event.getPlayer();
        if (!ClaimManager.isPlayerAllowed(player, ProtectionFlag.INTERACTION_BONEMEAL, event.getClickedBlock().getLocation())) {
            event.setCancelled(true);
            CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.interact"));
        }
    }
}
