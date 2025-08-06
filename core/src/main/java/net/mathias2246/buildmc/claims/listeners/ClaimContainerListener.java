package net.mathias2246.buildmc.claims.listeners;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.claims.Claim;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.claims.ProtectionFlag;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;

import java.sql.SQLException;
import java.util.EnumSet;

public class ClaimContainerListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onOpenContainer(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();

        // Block container access based on claim protection
        if (event.getInventory().getHolder() instanceof BlockState holder) {
            Claim claim = null;

            try {
                claim = ClaimManager.getClaim(holder.getLocation());
            } catch (SQLException e) {
                CoreMain.plugin.getLogger().severe("SQL error while getting claim: " + e.getMessage());
            }

            if (claim == null) return;

            if (claim.getWhitelistedPlayers().contains(player.getUniqueId())) return;

            if (claim.hasFlag(ProtectionFlag.CONTAINER)) {
                if (event.getInventory().getType() == InventoryType.ENDER_CHEST && claim.hasFlag(ProtectionFlag.ALLOW_ENDER_CHESTS)) {
                    return;
                }

                CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.container"));
                event.setCancelled(true);
            }
        } else if (event.getInventory().getHolder() instanceof Entity entity) {

            if (!ClaimManager.isPlayerAllowed(player, EnumSet.of(ProtectionFlag.CONTAINER), entity.getLocation())) {
                CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.entity-container"));
                event.setCancelled(true);
            }
        } else {
            // Fallback logic. Fails if player opens protected inventory from unprotected chunk
            if (!ClaimManager.isPlayerAllowed(player, EnumSet.of(ProtectionFlag.CONTAINER), player.getLocation())) {
                CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.entity-container"));
                event.setCancelled(true);
            }
        }
    }
}
