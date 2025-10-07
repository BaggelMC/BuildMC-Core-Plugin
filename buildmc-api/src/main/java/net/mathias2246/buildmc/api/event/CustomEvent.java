package net.mathias2246.buildmc.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Base class for custom Bukkit events that support metadata.
 * <p>
 * This class extends {@link Event} and delegates metadata handling
 * to an internal {@link MetadataHolder}. Metadata allows plugin developers
 * to attach arbitrary contextual data to events without subclassing or
 * creating wrapper types.
 * <p>
 * Subclasses must provide their own static {@link HandlerList} and
 * override {@link #getHandlers()} as required by the Bukkit event system.
 *
 * @see MetadataHolder
 * @see CustomPlayerEvent
 */
public abstract class CustomEvent extends Event {

    /** Internal holder for metadata entries. */
    protected final MetadataHolder metadataHolder = new MetadataHolder() {};

    /**
     * Returns an unmodifiable view of the metadata map associated with this event.
     * <p>
     * The returned map reflects all metadata currently attached to the event.
     * Use {@link #putMetadata(String, Object)} or {@link #removeMetadata(String)}
     * to modify entries.
     *
     * @return an unmodifiable map of metadata (never {@code null})
     */
    @NotNull
    public Map<String, Object> getMetadata() {
        return metadataHolder.getMetadata();
    }

    /**
     * Adds or updates a metadata entry for this event.
     *
     * @param key   the metadata key (must not be {@code null})
     * @param value the metadata value (must not be {@code null})
     * @throws NullPointerException if {@code key} or {@code value} is {@code null}
     */
    public void putMetadata(@NotNull String key, @NotNull Object value) {
        metadataHolder.putMetadata(key, value);
    }

    /**
     * Removes a metadata entry associated with the given key.
     * <p>
     * If no entry exists for the given key, this method does nothing.
     *
     * @param key the metadata key to remove (must not be {@code null})
     * @throws NullPointerException if {@code key} is {@code null}
     */
    public void removeMetadata(@NotNull String key) {
        metadataHolder.removeMetadata(key);
    }

    /**
     * Returns the {@link HandlerList} for this event type.
     * <p>
     * Bukkit requires each concrete event class to define its own static
     * {@code HandlerList} instance and to return it from this method.
     * This is necessary for Bukkit’s event registration and dispatch system.
     *
     * @return the handler list for this event (never {@code null})
     */
    @Override
    public abstract @NotNull HandlerList getHandlers();
}
