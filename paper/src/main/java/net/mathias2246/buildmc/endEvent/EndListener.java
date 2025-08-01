package net.mathias2246.buildmc.endEvent;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;

import static net.mathias2246.buildmc.Main.config;

public class EndListener implements Listener {

    public static boolean allowEnd = false;

    public static boolean disallowTntThroughEnd = true;

    public static void loadFromConfig() {
        allowEnd = config.getBoolean("end-event.allow-end", false);
        disallowTntThroughEnd = config.getBoolean("disallow-tnt-through-end");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityPortal(EntityPortalEvent event) {

        if (!event.getPortalType().equals(PortalType.ENDER)) return;

        if (allowEnd) {
            if (disallowTntThroughEnd) {
                Entity entity = event.getEntity();
                EntityType type = entity.getType();

                if (type == EntityType.TNT || type == EntityType.TNT_MINECART || type == EntityType.CREEPER) {
                    event.setCancelled(true);
                    return;
                }
            }
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPortal(PlayerPortalEvent event) {

        if (allowEnd) return;

        if (event.getTo().getWorld().getEnvironment() == World.Environment.THE_END) {
            event.setCancelled(true);

            Component message = Message.msg(event.getPlayer(), "messages.end-event.closed-message");
            event.getPlayer().sendActionBar(message);
        }
    }
}
