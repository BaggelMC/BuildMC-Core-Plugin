package net.mathias2246.buildmc.api.messageBus;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

public class EventChannel implements Keyed {
    @NotNull private final NamespacedKey key;
    @NotNull private final SemVer version;
    @NotNull private final String publisherId;

    public EventChannel(@NotNull NamespacedKey key, @NotNull SemVer version, @NotNull String publisherId) {
        this.key = key;
        this.version = version;
        this.publisherId = publisherId;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    public @NotNull SemVer getVersion() {
        return version;
    }

    public @NotNull String getPublisherId() {
        return publisherId;
    }
}
