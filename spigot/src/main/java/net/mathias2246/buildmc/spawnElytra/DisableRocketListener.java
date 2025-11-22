package net.mathias2246.buildmc.spawnElytra;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import static net.mathias2246.buildmc.Main.audiences;

public class DisableRocketListener implements Listener {
    // Disables the use of rockets to boost you when using the spawn-elytra to limit the distance you can easily reach
    @EventHandler
    public void onFireworkUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.FIREWORK_ROCKET) {

            if (!SpawnElytraUtil.isUsingSpawnElytra(player)) return;

            event.setCancelled(true);

            Component text = Message.msg(player, "messages.spawn-elytra.firework-disabled-hint");
            audiences.player(player).sendActionBar(text);
        }
    }

}
