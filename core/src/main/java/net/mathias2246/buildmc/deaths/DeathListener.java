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
        PlayerInventory inv = player.getInventory();

        ItemStack[] contents = inv.getContents().clone();
        ItemStack[] armor = inv.getArmorContents().clone();
        ItemStack offhand = inv.getItemInOffHand().clone();

        String deathMessage = event.getDeathMessage();
        int totalExp = player.getTotalExperience();
        var uuid = player.getUniqueId();

        CoreMain.plugin.getServer().getScheduler().runTaskAsynchronously(CoreMain.plugin, () -> {
            Map<Integer, byte[]> items = new HashMap<>();

            // inventory
            for (int i = 0; i < contents.length; i++) {
                ItemStack item = contents[i];
                if (item != null && item.getType() != Material.AIR) {
                    items.put(i, ItemStackSerialization.serialize(item));
                }
            }

            // armor slots (36–39)
            for (int i = 0; i < armor.length; i++) {
                ItemStack a = armor[i];
                if (a != null && a.getType() != Material.AIR) {
                    items.put(36 + i, ItemStackSerialization.serialize(a));
                }
            }

            // offhand (40)
            if (offhand.getType() != Material.AIR) {
                items.put(40, ItemStackSerialization.serialize(offhand));
            }

            try {
                CoreMain.deathTable.insertDeath(
                        CoreMain.databaseManager.getConnection(),
                        uuid,
                        totalExp,
                        deathMessage,
                        items
                );
            } catch (Exception e) {
                CoreMain.plugin.getLogger().severe(
                        "Error while adding Death to database: " + e.getMessage()
                );
            }
        });
    }
}
