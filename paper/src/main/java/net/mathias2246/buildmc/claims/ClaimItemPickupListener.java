package net.mathias2246.buildmc.claims;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

import static net.mathias2246.buildmc.Main.claimManager;

public class ClaimItemPickupListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemPickup(EntityPickupItemEvent event) {
        LivingEntity entity = event.getEntity();
        Item item = event.getItem();

        if (!(entity instanceof Player player)) return;

        if (!ClaimManager.isPlayerAllowed(claimManager, player, item.getLocation())) {
            event.setCancelled(true);
            player.sendActionBar(Component.translatable("messages.claims.not-accessible.item-pickup"));
        }

    }
}
