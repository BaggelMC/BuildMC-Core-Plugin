package net.mathias2246.buildmc.endEvent;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.util.config.ConfigurationValidationException;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;

import java.util.ArrayList;
import java.util.List;

import static net.mathias2246.buildmc.Main.config;

public class EndListener implements Listener {

    public static boolean allowEnd = false;

    private static List<EntityType> blockedEntities;

    public static void loadFromConfig() throws ConfigurationValidationException {
        allowEnd = config.getBoolean("end-event.allow-end", false);
        List<String> rawList = config.getStringList("end-event.blocked-entities");

        List<EntityType> validatedEntities = new ArrayList<>();

        for (String name : rawList) {
            String upperName = name.toUpperCase();
            try {
                EntityType type = EntityType.valueOf(upperName);
                validatedEntities.add(type);
            } catch (IllegalArgumentException e) {
                throw new ConfigurationValidationException("Invalid entity type in end-event.blocked-entities: " + name);
            }
        }

        blockedEntities = validatedEntities;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityPortal(EntityPortalEvent event) {
        if (!event.getPortalType().equals(PortalType.ENDER)) return;

        if (allowEnd) {
            if (blockedEntities.contains(event.getEntity().getType())) {
                event.setCancelled(true);
            }
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPortal(PlayerPortalEvent event) {

        if (allowEnd) return;

        if (event.getTo().getWorld().getEnvironment() == World.Environment.THE_END) {
            event.setCancelled(true);

            event.getPlayer().sendActionBar(Component.translatable("messages.end-event.closed-message"));
        }
    }
}
