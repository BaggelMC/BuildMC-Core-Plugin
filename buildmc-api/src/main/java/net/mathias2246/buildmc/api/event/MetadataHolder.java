package net.mathias2246.buildmc.api.event;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class providing metadata support for custom events.
 * <p>
 * Metadata allows additional context to be stored and shared with plugins
 * without needing to subclass the event. Each entry is a key-value pair.
 * Keys must be unique within a single event instance.
 * <p>
 * This class provides an unmodifiable view for external access to prevent
 * accidental modification of the internal state.
 */
public abstract class MetadataHolder {

    /** Internal storage for metadata entries. */
    private final Map<String, Object> metadata = new HashMap<>();

    /**
     * Returns an unmodifiable view of all metadata entries associated with this event.
     * <p>
     * This map should not be modified directly; use {@link #putMetadata(String, Object)}
     * or {@link #removeMetadata(String)} to update entries.
     *
     * @return an unmodifiable map containing all metadata (never {@code null})
     */
    public @NotNull Map<String, Object> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    /**
     * Provides internal access to the mutable metadata map.
     * <p>
     * Intended for subclasses or internal use only. Modifications to this map
     * will affect the event's metadata directly.
     *
     * @return the mutable map of metadata entries (never {@code null})
     */
    protected Map<String, Object> mutableMetadata() {
        return metadata;
    }

    /**
     * Adds a new metadata entry or updates the value for an existing key.
     *
     * @param key   the metadata key (must not be {@code null})
     * @param value the metadata value (must not be {@code null})
     * @throws NullPointerException if {@code key} or {@code value} is {@code null}
     */
    public void putMetadata(@NotNull String key, @NotNull Object value) {
        metadata.put(key, value);
    }

    /**
     * Removes a metadata entry from the event.
     * <p>
     * If the key does not exist, this method does nothing.
     *
     * @param key the metadata key to remove (must not be {@code null})
     * @throws NullPointerException if {@code key} is {@code null}
     */
    public void removeMetadata(@NotNull String key) {
        metadata.remove(key);
    }
}
