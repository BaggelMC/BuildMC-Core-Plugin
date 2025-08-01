package net.mathias2246.buildmc.claims;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import static net.mathias2246.buildmc.Main.claimManager;

public class ClaimListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplosion(EntityExplodeEvent event) {
        if (ClaimManager.hasOwner(event.getLocation().getChunk())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplosion(BlockExplodeEvent event) {
        if (ClaimManager.hasOwner(event.getExplodedBlockState().getChunk())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (!ClaimManager.isPlayerAllowed(claimManager, player, event.getBlock().getLocation())) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerMine(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (!ClaimManager.isPlayerAllowed(claimManager, player, event.getBlock().getLocation())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onOpenContainer(InventoryOpenEvent event) {
        HumanEntity player = event.getPlayer();
        if (!(ClaimManager.isPlayerAllowed(claimManager, player, event.getPlayer().getLocation()))) event.setCancelled(true);
    }
}
