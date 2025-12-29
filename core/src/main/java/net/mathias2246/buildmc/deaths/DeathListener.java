package net.mathias2246.buildmc.deaths;

import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.util.ItemStackSerialization;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;

public class DeathListener implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        Map<Integer, byte[]> items = new HashMap<>();

        PlayerInventory inv = player.getInventory();

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null) {
                items.put(i, ItemStackSerialization.serialize(item));
            }
        }

        // armor
        for (int i = 0; i < 4; i++) {
            ItemStack armor = inv.getArmorContents()[i];
            if (armor != null) {
                items.put(36 + i, ItemStackSerialization.serialize(armor));
            }
        }

        // offhand
        ItemStack offhand = inv.getItemInOffHand();
        if (offhand.getType() != Material.AIR) {
            items.put(40, ItemStackSerialization.serialize(offhand));
        }

        try {
            CoreMain.deathTable.insertDeath(
                    CoreMain.databaseManager.getConnection(),
                    player.getUniqueId(),
                    player.getTotalExperience(),
                    event.getDeathMessage(),
                    items
            );
        } catch (Exception e) {
            CoreMain.plugin.getLogger().severe("Error while adding Death to database: " + e.getMessage());
        }

    }
}
