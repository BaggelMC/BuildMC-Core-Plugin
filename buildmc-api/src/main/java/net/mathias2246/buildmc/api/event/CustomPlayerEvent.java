package net.mathias2246.buildmc.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public abstract class CustomPlayerEvent extends PlayerEvent {

    public CustomPlayerEvent(@NotNull Player player) {
        super(player);
    }

    private final MetadataHolder metadataHolder = new MetadataHolder() {};

    @NotNull
    public Map<String, Object> getMetadata() {
        return metadataHolder.getMetadata();
    }

    public void putMetadata(@NotNull String key, @NotNull Object value) {
        metadataHolder.putMetadata(key, value);
    }

    public void removeMetadata(@NotNull String key) {
        metadataHolder.removeMetadata(key);
    }

    // HandlerList stays abstract. All subclasses must define their own due to Bukkit internals.
    @Override
    public abstract @NotNull HandlerList getHandlers();
}
