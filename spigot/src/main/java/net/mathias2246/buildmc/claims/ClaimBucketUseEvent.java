package net.mathias2246.buildmc.claims;

import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

import static net.mathias2246.buildmc.Main.audiences;
import static net.mathias2246.buildmc.Main.claimManager;

public class ClaimBucketUseEvent implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (!ClaimManager.isPlayerAllowed(claimManager, player, block.getLocation())) {
            audiences.player(player).sendActionBar(Component.translatable("messages.claims.not-accessible.block-place"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (!ClaimManager.isPlayerAllowed(claimManager, player, block.getLocation())) {
            audiences.player(player).sendActionBar(Component.translatable("messages.claims.not-accessible.block-break"));
            event.setCancelled(true);
        }
    }
}
