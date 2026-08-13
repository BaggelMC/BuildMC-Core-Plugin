package net.mathias2246.buildmc.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class providing metadata storage.
 * <p>
 * Metadata allows additional context to be stored and shared with plugins.
 * Each entry is a key-value pair.
 * Keys must be unique within a metadata holder instance.
 * <p>
 * This class provides an unmodifiable view for external access to prevent
 * accidental modification of the internal state.
 */
public abstract class MetadataHolder {

    /** Internal storage for metadata entries. */
    private final @NotNull Map<String, Object> metadata = new HashMap<>();

    /**
     * Returns an unmodifiable view of all metadata entries stored in this instance.
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
     * will affect the instance's metadata directly.
     *
     * @return the mutable map of metadata entries (never {@code null})
     */
    protected @NotNull Map<String, Object> mutableMetadata() {
        return metadata;
    }

    /**
     * Checks if a certain metadata key is present in this instance.
     *
     * @param key The key to check
     * @return {@code true} if the given metadata key is present in this instance.
     * **/
    @Contract(pure = true)
    public boolean containsMetadataKey(@NotNull String key) {
        return metadata.containsKey(key);
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
     * Removes a metadata entry from this instance.
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
