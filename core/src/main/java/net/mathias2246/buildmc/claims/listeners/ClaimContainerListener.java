package net.mathias2246.buildmc.claims.listeners;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.claims.Claim;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.claims.ProtectionFlag;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Lectern;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;

import java.sql.SQLException;

public class ClaimContainerListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onOpenContainer(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();

        Location loc = event.getInventory().getLocation();

        if (loc == null) return;

        Claim claim;
        try {
            claim = ClaimManager.getClaim(loc);
        } catch (SQLException e) {
            CoreMain.plugin.getLogger().severe("SQL error while getting claim: " + e);
            return;
        }
        if (claim == null) return;


        if (event.getInventory().getHolder() instanceof Lectern) return;

        if (event.getView().getType().equals(InventoryType.ENDER_CHEST)) return;

        if (event.getInventory().getHolder() instanceof BlockState block) {

            if (claim.getWhitelistedPlayers().contains(player.getUniqueId())) return;

            if (!ClaimManager.isPlayerAllowed(player, ProtectionFlag.CONTAINER, claim)) {
                CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.container"));
                event.setCancelled(true);
            }

        } else if (event.getInventory().getHolder() instanceof Entity entity) {

            if (!ClaimManager.isPlayerAllowed(player, ProtectionFlag.CONTAINER, claim)) {
                CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.entity-container"));
                event.setCancelled(true);
            }

        } else {
            if (!ClaimManager.isPlayerAllowed(player, ProtectionFlag.CONTAINER, claim)) {
                CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.container"));
                event.setCancelled(true);
            }

        }
    }

    @EventHandler
    public void onLecternTakeBook(PlayerTakeLecternBookEvent event) {
        var block = event.getLectern();
        var player = event.getPlayer();

        Claim claim ;
        try {
            claim = ClaimManager.getClaim(block.getLocation());
        } catch (SQLException e) {
            CoreMain.plugin.getLogger().severe("SQL error while getting claim: " + e);
            return;
        }
        if (claim == null) return;

        if (claim.hasFlag(ProtectionFlag.CONTAINER) && !ClaimManager.isPlayerAllowed(player, ProtectionFlag.CONTAINER, claim)) {
            event.setCancelled(true);
            CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.interact"));
        }
    }
}
