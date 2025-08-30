package net.mathias2246.buildmc.playerHeads;

import net.mathias2246.buildmc.api.event.playerheads.PlayerHeadDropEvent;
import net.mathias2246.buildmc.api.item.ItemMetaModifier;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

/**An Event Listener that when register makes it so players drop their head on death.*/
public record PlayerHeadDropDeathListener(@Nullable ItemMetaModifier modifier) implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        Entity killer = event.getDamageSource().getCausingEntity();

        if (killer == null) return;

        if (!(killer instanceof Player killerPlayer)) return;

        if (event.getKeepInventory()) return;

        var playerHead = new ItemStack(Material.PLAYER_HEAD);

        if (!(playerHead.getItemMeta() instanceof SkullMeta meta)) return;

        if (modifier != null) modifier.modifyMeta(meta, player, event);
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()));
        playerHead.setItemMeta(meta);

        PlayerHeadDropEvent headDropEvent = new PlayerHeadDropEvent(player, killerPlayer, playerHead);

        Bukkit.getPluginManager().callEvent(headDropEvent);

        if (headDropEvent.isCancelled()) return;

        ItemStack newPlayerHead = headDropEvent.getPlayerHead();

        event.getDrops().add(
                newPlayerHead
        );

    }
}
