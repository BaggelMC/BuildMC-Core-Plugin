package net.mathias2246.buildmc.endEvent;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;

import static net.mathias2246.buildmc.Main.config;

public class EndListener implements Listener {

    public static boolean allowEnd = false;

    public static void loadFromConfig() {
        allowEnd = config.getBoolean("end-event.allow-end", false);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPortalEvent(EntityPortalEvent event) {
        if (!event.getFrom().getBlock().getType().equals(Material.END_PORTAL)) return;
        if (!allowEnd) event.setCancelled(true);
    }
}
