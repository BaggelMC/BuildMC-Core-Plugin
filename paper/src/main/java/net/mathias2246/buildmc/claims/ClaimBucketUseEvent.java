package net.mathias2246.buildmc.claims;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

import static net.mathias2246.buildmc.Main.claimManager;

public class ClaimBucketUseEvent implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucketUse(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null) return;

        Material type = item.getType();
        Block block = event.getClickedBlock();

        if (block == null) return;

        boolean isBucketUse = type == Material.WATER_BUCKET ||
                type == Material.LAVA_BUCKET ||
                type == Material.POWDER_SNOW_BUCKET ||
                type == Material.BUCKET ||
                type == Material.AXOLOTL_BUCKET ||
                type == Material.TADPOLE_BUCKET ||
                type == Material.PUFFERFISH_BUCKET ||
                type == Material.TROPICAL_FISH_BUCKET ||
                type == Material.SALMON_BUCKET ||
                type == Material.COD_BUCKET;

        if (!isBucketUse) return;

        if (!ClaimManager.isPlayerAllowed(claimManager, player, block.getLocation())) {
            player.sendActionBar(Component.translatable("messages.claims.not-accessible.block-place"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityPickup(PlayerBucketEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getEntity();

        if (!ClaimManager.isPlayerAllowed(claimManager, player, entity.getLocation())) {
            player.sendActionBar(Message.msg(player, "messages.claims.not-accessible.entity-bucket", Map.of("entity", entity.getName())));
            event.setCancelled(true);
        }
    }
}
