package net.mathias2246.buildmc.claims.listeners;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.claims.ClaimManager;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;

public class ClaimContainerListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onOpenContainer(InventoryOpenEvent event) {

        if (event.getInventory().getType().equals(InventoryType.ENDER_CHEST) || event.getInventory().getType().equals(InventoryType.LECTERN)) return;

        Player player = (Player) event.getPlayer();

        Location loc = event.getInventory().getLocation();

        if (loc == null) return;

        // Block container access based on claim protection
        // Ender chests should not return a holder
        if (event.getInventory().getHolder() instanceof BlockState holder) {
            if (ClaimManager.isPlayerAllowed(CoreMain.claimManager, player, holder.getLocation())) return;
            CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.container"));
            event.setCancelled(true);
        } else if (event.getInventory().getHolder() instanceof Entity entity) {

            if (!ClaimManager.isPlayerAllowed(CoreMain.claimManager, player, entity.getLocation())) {
                CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.entity-container"));
                event.setCancelled(true);
            }
        } else {
            // Fallback logic.
            if (!ClaimManager.isPlayerAllowed(CoreMain.claimManager, player, event.getInventory().getLocation())) {
                CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.container"));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onLecternTakeBook(PlayerTakeLecternBookEvent event) {
        Player player = event.getPlayer();
        if (!ClaimManager.isPlayerAllowed(CoreMain.claimManager, player, event.getLectern().getLocation())) event.setCancelled(true);
    }
}
