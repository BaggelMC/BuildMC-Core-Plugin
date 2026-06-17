package net.mathias2246.buildmc.deaths;

import net.mathias2246.buildmc.player.status.PlayerStatusUtil;
import net.mathias2246.buildmc.util.ItemStackSerialization;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

import static net.mathias2246.buildmc.CoreMain.config;

public class DeathRestoreUtil {

    // TODO: Try make async
    // TODO: Create better ItemStack serialization / deserialization
    public static void restore(Player player, DeathRecord record, boolean updateStatistics) {
        player.giveExp(record.xp());

        for (Map.Entry<Integer, byte[]> entry : record.items().entrySet()) {
            int slot = entry.getKey();
            byte[] bytes = entry.getValue();
            ItemStack item = ItemStackSerialization.deserialize(bytes);

            if (slot < 36) {
                ItemStack existing = player.getInventory().getItem(slot);
                if (existing != null && !existing.getType().isAir()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                } else {
                    player.getInventory().setItem(slot, item);
                }
            } else if (slot < 40) {

                ItemStack[] armor = player.getInventory().getArmorContents();
                int armorSlot = slot - 36;

                if (armor[armorSlot] != null && !armor[armorSlot].getType().isAir()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                } else {
                    armor[armorSlot] = item;
                }

                player.getInventory().setArmorContents(armor);

            } else if (slot == 40) {
                ItemStack offhand = player.getInventory().getItemInOffHand();
                if (!offhand.getType().isAir()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                } else {
                    player.getInventory().setItemInOffHand(item);
                }
            }
        }

        if (updateStatistics) {
            int currentDeaths = player.getStatistic(Statistic.DEATHS);
            if (currentDeaths > 0) {
                player.setStatistic(Statistic.DEATHS, currentDeaths - 1);
            }
            if (config.getBoolean("status.death-counter", false)) {
                if (PlayerStatusUtil.hasStatus(player)) {
                    PlayerStatusUtil.reloadPlayerStatus(player);
                } else {
                    PlayerStatusUtil.refreshTabListNameNoStatus(player);
                }
            }
        }
    }
}
