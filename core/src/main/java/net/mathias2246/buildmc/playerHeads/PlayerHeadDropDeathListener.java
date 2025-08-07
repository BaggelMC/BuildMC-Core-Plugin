package net.mathias2246.buildmc.playerHeads;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

/**An Event Listener that when register makes it so players drop their head on death.*/
public class PlayerHeadDropDeathListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (event.getKeepInventory()) return;
        var i = new ItemStack(Material.PLAYER_HEAD);
        if (i.getItemMeta() instanceof SkullMeta meta) {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()));
            i.setItemMeta(meta);
            event.getDrops().add(
                    i
            );
        }


    }
}
