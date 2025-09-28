package net.mathias2246.buildmc.api.event;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface EventMetadata {
    /**
     * Gets an unmodifiable view of the metadata map associated with this event.
     * <p>
     * Metadata can be used by plugins to share additional context between event
     * listeners without subclassing this event.
     *
     * @return an unmodifiable map of metadata (never {@code null})
     */
    @NotNull
    Map<String, Object> getMetadata();

    /**
     * Adds or updates a metadata entry.
     *
     * @param key   the metadata key (never {@code null})
     * @param value the metadata value (never {@code null})
     */
    void putMetadata(@NotNull String key, @NotNull Object value);

    /**
     * Removes a metadata entry.
     *
     * @param key the metadata key to remove (never {@code null})
     */
    void removeMetadata(@NotNull String key);
}
