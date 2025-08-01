package net.mathias2246.buildmc.claims;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import static net.mathias2246.buildmc.Main.claimManager;

public class ClaimBreakListener implements Listener {

    @EventHandler
    public void onPlayerMine(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (!ClaimManager.isPlayerAllowed(claimManager, player, event.getBlock().getLocation())) {
            player.sendActionBar(Component.translatable("messages.claims.not-accessible.block-break"));
            event.setCancelled(true);
        }
    }
}
