package net.mathias2246.buildmc.endEvent;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.Main;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;

import java.util.List;

import static net.mathias2246.buildmc.Main.config;

public class EndListener implements Listener {

    public static boolean allowEnd = false;
    private static List<String> blockedEntities;

    public static void loadFromConfig() {
        allowEnd = config.getBoolean("end-event.allow-end", false);
        blockedEntities = config.getStringList("end-event.blocked-entities");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityPortal(EntityPortalEvent event) {
        Entity entity = event.getEntity();
        EntityType type = entity.getType();

        World fromWorld = entity.getWorld();
        World toWorld = event.getTo() != null ? event.getTo().getWorld() : null;
        if (toWorld == null) return;

        if (allowEnd) {
            if ((fromWorld.getEnvironment() == World.Environment.THE_END ||
                    (toWorld.getEnvironment() == World.Environment.THE_END)) &&
                    blockedEntities.contains(type.name())) {

                event.setCancelled(true);
            }
        } else {

            if (toWorld.getEnvironment() == World.Environment.THE_END) {
                event.setCancelled(true);
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (allowEnd) return;
        if (event.getPlayer().hasPermission("buildmc.bypass-end-event")) return;

        if (event.getTo() == null || event.getTo().getWorld() == null) return;

        if (event.getTo().getWorld().getEnvironment() == World.Environment.THE_END) {
            event.setCancelled(true);

            Component message = Message.msg(event.getPlayer(), "messages.end-event.closed-message");
            Main.audiences.player(event.getPlayer()).sendActionBar(message);
        }
    }
}
