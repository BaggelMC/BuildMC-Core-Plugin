package net.mathias2246.buildmc.claims;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;

import static net.mathias2246.buildmc.Main.audiences;
import static net.mathias2246.buildmc.Main.claimManager;

public class ClaimContainerListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onOpenContainer(InventoryOpenEvent event) {
        HumanEntity player = event.getPlayer();

        if (event.getInventory().getHolder() instanceof org.bukkit.block.BlockState holder) {
            if (!ClaimManager.isPlayerAllowed(claimManager, player, holder.getLocation())) {
                audiences.player((Player) player).sendActionBar(Component.translatable("messages.claims.not-accessible.container"));
                event.setCancelled(true);
            }
        } else if (event.getInventory().getHolder() instanceof org.bukkit.entity.Entity entity) {
            if (!ClaimManager.isPlayerAllowed(claimManager, player, entity.getLocation())) {
                audiences.player((Player)player).sendActionBar(Component.translatable("messages.claims.not-accessible.entity-container"));
                event.setCancelled(true);
            }
        } else {
            // Fallback logic. Fails if player opens protected inventory from unprotected chunk
            if (!ClaimManager.isPlayerAllowed(claimManager, player, player.getLocation())) {
                audiences.player((Player)player).sendActionBar(Component.translatable("messages.claims.not-accessible.container"));
                event.setCancelled(true);
            }
        }
    }
}
