package net.mathias2246.buildmc.claims;

import net.kyori.adventure.text.Component;
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

        if (!ClaimManager.isPlayerAllowed(claimManager, player, event.getBlock().getLocation())) {
            player.sendActionBar(Component.translatable("messages.claims.not-accessible.block-place"));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMine(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (!ClaimManager.isPlayerAllowed(claimManager, player, event.getBlock().getLocation())) {
            player.sendActionBar(Component.translatable("messages.claims.not-accessible.block-break"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onOpenContainer(InventoryOpenEvent event) {
        HumanEntity player = event.getPlayer();

        if (event.getInventory().getHolder() instanceof org.bukkit.block.BlockState holder) {
            if (!ClaimManager.isPlayerAllowed(claimManager, player, holder.getLocation())) {
                player.sendActionBar(Component.translatable("messages.claims.not-accessible.container"));
                event.setCancelled(true);
            }
        } else if (event.getInventory().getHolder() instanceof org.bukkit.entity.Entity entity) {
            if (!ClaimManager.isPlayerAllowed(claimManager, player, entity.getLocation())) {
                player.sendActionBar(Component.translatable("messages.claims.not-accessible.entity-container"));
                event.setCancelled(true);
            }
        } else {
            // Fallback logic. Fails if player opens protected inventory from unprotected chunk
            if (!ClaimManager.isPlayerAllowed(claimManager, player, player.getLocation())) {
                player.sendActionBar(Component.translatable("messages.claims.not-accessible.container"));
                event.setCancelled(true);
            }
        }
    }
}
