package net.mathias2246.buildmc.claims;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.Main;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;

import static net.mathias2246.buildmc.Main.claimManager;

public class ClaimContainerListener implements Listener {

    private final boolean allowEnderChests = Main.config.getBoolean("claims.protections.allow-ender-chests");

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onOpenContainer(InventoryOpenEvent event) {
        HumanEntity player = event.getPlayer();

        // Allow Ender Chest if setting is enabled
        if (event.getInventory().getType() == org.bukkit.event.inventory.InventoryType.ENDER_CHEST) {
            if (allowEnderChests) return;

            player.sendActionBar(Component.translatable("messages.claims.not-accessible.container"));
            event.setCancelled(true);
            return;
        }

        // Block container access based on claim protection
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
