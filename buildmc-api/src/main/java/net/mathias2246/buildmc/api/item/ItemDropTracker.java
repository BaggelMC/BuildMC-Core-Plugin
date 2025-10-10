package net.mathias2246.buildmc.api.item;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks players who recently dropped an item.
 * <p>
 * Uses high-resolution timestamps to determine if a player dropped
 * an item within a configurable threshold (default 100 ms).
 */
@ApiStatus.Internal
public class ItemDropTracker implements Listener {

    private static ItemDropTracker instance;

    /** Last drop timestamp (nanoTime) per player */
    private final Map<UUID, Long> lastDropNano = new HashMap<>();

    /** Threshold considered "same tick" (default = 100 ms = 100_000_000 ns) */
    private final long sameTickNanos;

    public ItemDropTracker(Plugin plugin) {
        this(plugin, 100_000_000L);
    }

    public ItemDropTracker(Plugin plugin, long sameTickNanos) {
        if (instance != null) {
            throw new IllegalStateException("ItemDropTracker already initialized!");
        }
        instance = this;
        this.sameTickNanos = sameTickNanos;

        Bukkit.getPluginManager().registerEvents(this, plugin);

        // cleanup
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long cutoff = System.nanoTime() - sameTickNanos * 10; // buffer
            lastDropNano.entrySet().removeIf(e -> e.getValue() < cutoff);
        }, 20L * 30L, 20L * 30L); // every 30 seconds
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onItemDrop(PlayerDropItemEvent event) {
        lastDropNano.put(event.getPlayer().getUniqueId(), System.nanoTime());
    }

    /**
     * Returns true if the player dropped an item within the configured threshold.
     */
    public static boolean droppedRecently(Player player) {
        if (instance == null) return false;
        Long last = instance.lastDropNano.get(player.getUniqueId());
        return last != null && (System.nanoTime() - last) <= instance.sameTickNanos;
    }
}
