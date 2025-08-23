package net.mathias2246.buildmc.claims.listeners;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.claims.ProtectionFlag;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.inventory.ItemStack;

public class ClaimFrostWalkerListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFrostWalkerUse(EntityBlockFormEvent event) {
        if (event.getNewState().getType() != Material.FROSTED_ICE) return;

        Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;

        ItemStack boots = player.getInventory().getBoots();
        if (boots == null || !boots.containsEnchantment(Enchantment.FROST_WALKER)) return;

        if (!ClaimManager.isPlayerAllowed(player, ProtectionFlag.FROST_WALKER, event.getBlock().getLocation())) {
            event.setCancelled(true);
            CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.frostwalker"));
        }
    }
}
