package net.mathias2246.buildmc.claims;


import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import static net.mathias2246.buildmc.CoreMain.audiences;
import static net.mathias2246.buildmc.CoreMain.claimManager;

public class ClaimItemDropListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (!ClaimManager.isPlayerAllowed(claimManager, player, player.getLocation())) {
            audiences.player(player).sendActionBar(Component.translatable("messages.claims.not-accessible.item-drop"));
            event.setCancelled(true);
        }

    }
}
