package net.mathias2246.buildmc.claims.listeners;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.claims.Claim;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.claims.ProtectionFlag;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.sql.SQLException;

public class ClaimInteractionListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        Block block = event.getClickedBlock();
        Material type = block.getType();

        if (!type.isInteractable()) return;

        Player player = event.getPlayer();

        // Check if it's an interactable block we want to restrict
        ProtectionFlag flag = getProtectionFlagFor(type);
        if (flag == null) return;

        Claim claim;
        try {
            claim = ClaimManager.getClaim(block.getLocation());
        } catch (SQLException e) {
            CoreMain.plugin.getLogger().severe("SQL error while getting claim: " + e);
            return;
        }
        if (claim == null) return;

        if (claim.hasFlag(flag) && !ClaimManager.isPlayerAllowed(player, flag, claim)) {
            event.setCancelled(true);
            CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.interact"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        //if (event.getBlock().getType() != Material.FARMLAND) return;

        Claim claim = null;
        try {
            claim = ClaimManager.getClaim(event.getBlock().getLocation());
        } catch (SQLException e) {
            CoreMain.plugin.getLogger().severe("SQL error while getting claim: " + e);
        }
        if (claim == null) return;

        if (claim.hasFlag(ProtectionFlag.INTERACTION_FARMLAND) && !ClaimManager.isPlayerAllowed(player, ProtectionFlag.INTERACTION_FARMLAND, claim)) {
            event.setCancelled(true);
            CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.interact"));
        }
    }

    private ProtectionFlag getProtectionFlagFor(Material type) {

        if (type.equals(Material.LEVER)) return ProtectionFlag.INTERACTION_LEVERS;
        if (Tag.BUTTONS.isTagged(type)) return ProtectionFlag.INTERACTION_BUTTONS;
        if (Tag.PRESSURE_PLATES.isTagged(type)) return ProtectionFlag.INTERACTION_PRESSURE_PLATES;
        if (type.equals(Material.REPEATER)) return ProtectionFlag.INTERACTION_REPEATERS;
        if (type.equals(Material.COMPARATOR)) return ProtectionFlag.INTERACTION_COMPARATORS;
        if (Tag.DOORS.isTagged(type)) return ProtectionFlag.INTERACTION_TRAPDOORS;
        if (Tag.TRAPDOORS.isTagged(type)) return ProtectionFlag.INTERACTION_DOORS;
        if (Tag.FENCE_GATES.isTagged(type)) return ProtectionFlag.INTERACTION_FENCE_GATES;
        if (Tag.CANDLES.isTagged(type)) return ProtectionFlag.INTERACTION_CANDLES;
        if (type.equals(Material.TNT)) return ProtectionFlag.INTERACTION_LIGHT_TNT;
        return null;
    }
}
