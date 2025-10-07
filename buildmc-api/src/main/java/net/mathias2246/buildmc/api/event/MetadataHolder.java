package net.mathias2246.buildmc.api.event;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class MetadataHolder {
    private final Map<String, Object> metadata = new HashMap<>();

    public @NotNull Map<String, Object> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    protected Map<String, Object> mutableMetadata() {
        return metadata; // internal access for mutation
    }

    public void putMetadata(@NotNull String key, @NotNull Object value) {
        metadata.put(key, value);
    }

    public void removeMetadata(@NotNull String key) {
        metadata.remove(key);
    }
}
