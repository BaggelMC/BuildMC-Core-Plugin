package net.mathias2246.buildmc.claims;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.inventory.ItemStack;

import static net.mathias2246.buildmc.CoreMain.audiences;
import static net.mathias2246.buildmc.CoreMain.claimManager;
import static net.mathias2246.buildmc.Main.audiences;
import static net.mathias2246.buildmc.Main.claimManager;

public class ClaimFrostWalkerListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFrostWalkerUse(EntityBlockFormEvent event) {
        if (event.getNewState().getType() != Material.FROSTED_ICE) return;

        Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;

        ItemStack boots = player.getInventory().getBoots();
        if (boots == null || !boots.containsEnchantment(Enchantment.FROST_WALKER)) return;

        // Check if player has permission to modify this location
        if (!ClaimManager.isPlayerAllowed(claimManager, player, event.getBlock().getLocation())) {
            event.setCancelled(true);
            audiences.player(player).sendActionBar(
                    Component.translatable("messages.claims.not-accessible.frostwalker")
            );
        }
    }
}
