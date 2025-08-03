package net.mathias2246.buildmc.claims;

import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;

import static net.mathias2246.buildmc.Main.audiences;
import static net.mathias2246.buildmc.Main.claimManager;

public class ClaimPlaceListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (!ClaimManager.isPlayerAllowed(claimManager, player, event.getBlock().getLocation())) {
            audiences.player(player).sendActionBar(Component.translatable("messages.claims.not-accessible.block-place"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (!ClaimManager.isPlayerAllowed(claimManager, player, block.getLocation())) {
            audiences.player(player).sendActionBar(Component.translatable("messages.claims.not-accessible.block-place"));
            event.setCancelled(true);
        }
    }
}
