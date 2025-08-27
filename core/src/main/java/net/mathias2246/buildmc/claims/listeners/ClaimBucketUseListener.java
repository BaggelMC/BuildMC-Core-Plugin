package net.mathias2246.buildmc.claims.listeners;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.claims.ProtectionFlag;
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

public class ClaimBucketUseListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucketUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null) return;

        Material type = item.getType();
        Block block = event.getClickedBlock();

        if (block == null) return;

        boolean isBucketUse = switch (type) {
            case WATER_BUCKET, LAVA_BUCKET, POWDER_SNOW_BUCKET, BUCKET,
                 AXOLOTL_BUCKET, TADPOLE_BUCKET, PUFFERFISH_BUCKET,
                 TROPICAL_FISH_BUCKET, SALMON_BUCKET, COD_BUCKET -> true;
            default -> false;
        };

        if (!isBucketUse) return;

        if (!ClaimManager.isPlayerAllowed(player, ProtectionFlag.BUCKET_USAGE, block.getLocation())) {
            CoreMain.mainClass.sendPlayerActionBar(player,
                    Component.translatable("messages.claims.not-accessible.block-place"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityPickup(PlayerBucketEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getEntity();

        if (!ClaimManager.isPlayerAllowed(player, ProtectionFlag.BUCKET_USAGE, entity.getLocation())) {
            CoreMain.mainClass.sendPlayerActionBar(player,
                    Component.translatable("messages.claims.not-accessible.entity-bucket"));
            event.setCancelled(true);
        }
    }
}
