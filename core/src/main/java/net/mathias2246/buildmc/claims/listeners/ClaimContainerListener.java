package net.mathias2246.buildmc.claims.listeners;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.claims.Claim;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.claims.ClaimType;
import net.mathias2246.buildmc.claims.ProtectionFlag;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.scoreboard.Team;

import java.sql.SQLException;
import java.util.EnumSet;
import java.util.Objects;

public class ClaimContainerListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onOpenContainer(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();

        Location loc = event.getInventory().getLocation();

        if (loc == null) return;

        Claim claim = null;
        try {
            claim = ClaimManager.getClaim(loc);
        } catch (SQLException e) {
            CoreMain.plugin.getLogger().severe("SQL error while getting claim: " + e.getMessage());
        }
        if (claim == null) return;

        // Block container access based on claim protection
        // Ender chests should not return a holder
        if (event.getInventory().getHolder() instanceof BlockState holder) {
            if (claim.getWhitelistedPlayers().contains(player.getUniqueId())) return;

            if (claim.hasFlag(ProtectionFlag.CONTAINER)) {
                if (claim.getType() == ClaimType.PLAYER) {
                    if (Objects.equals(claim.getOwnerId(), player.getUniqueId().toString())) return;
                }
                if (claim.getType() == ClaimType.TEAM) {
                    Team team = ClaimManager.getPlayerTeam(player);
                    if (team != null) {
                        if (Objects.equals(claim.getOwnerId(), team.getName())) return;
                    }
                }

                CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.container"));
                event.setCancelled(true);
            }
        } else if (event.getInventory().getHolder() instanceof Entity entity) {

            if (!ClaimManager.isPlayerAllowed(player, EnumSet.of(ProtectionFlag.CONTAINER), claim)) {
                CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.entity-container"));
                event.setCancelled(true);
            }
        } else {
            // Fallback logic.
            if (event.getInventory().getType() == InventoryType.ENDER_CHEST && claim.hasFlag(ProtectionFlag.ALLOW_ENDER_CHESTS)) {
                return;
            }
            if (!ClaimManager.isPlayerAllowed(player, EnumSet.of(ProtectionFlag.CONTAINER), claim)) {
                CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.container"));
                event.setCancelled(true);
            }
        }
    }
}
