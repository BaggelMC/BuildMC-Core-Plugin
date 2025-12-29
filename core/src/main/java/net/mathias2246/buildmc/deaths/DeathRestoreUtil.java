package net.mathias2246.buildmc.deaths;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class DeathRestoreUtil {

    public static void restore(Player player, DeathRecord record) {

        player.setLevel(0);
        player.setExp(0f);
        player.giveExp(record.getXp());

        for (Map.Entry<Integer, byte[]> entry : record.getItems().entrySet()) {
            int slot = entry.getKey();
            byte[] bytes = entry.getValue();

            String json = new String(bytes, StandardCharsets.UTF_8);
            Map<String, Object> map = new com.google.gson.Gson().fromJson(json, Map.class);
            ItemStack item = ItemStack.deserialize(map);

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
                if (offhand != null && !offhand.getType().isAir()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                } else {
                    player.getInventory().setItemInOffHand(item);
                }
            }
        }
    }
}
