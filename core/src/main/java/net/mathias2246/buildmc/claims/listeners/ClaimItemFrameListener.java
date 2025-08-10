package net.mathias2246.buildmc.claims.listeners;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.claims.ClaimManager;
import org.bukkit.entity.GlowItemFrame;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class ClaimItemFrameListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemFrameInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ItemFrame itemFrame) && !(event.getRightClicked() instanceof GlowItemFrame glowFrame)) return;

        Player player = event.getPlayer();
        if (!ClaimManager.isPlayerAllowed(CoreMain.claimManager, player, event.getRightClicked().getLocation())) {
            event.setCancelled(true);
            CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.item-frame"));
        }
    }
    @EventHandler
    public void onItemFrameDestroy(HangingBreakByEntityEvent event) {
        if (!(event.getEntity() instanceof ItemFrame itemFrame) && !(event.getEntity() instanceof GlowItemFrame glowFrame)) return;

        if (!(event.getRemover() instanceof Player player)) return;

        if (!ClaimManager.isPlayerAllowed(CoreMain.claimManager, player, event.getEntity().getLocation())) {
            event.setCancelled(true);
            CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.item-frame"));
        }
    }
}
