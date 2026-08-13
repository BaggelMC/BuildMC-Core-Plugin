package net.mathias2246.buildmc.api.messageBus;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Contract;
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

    @Contract(pure = true)
    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    @Contract(pure = true)
    public @NotNull SemVer getVersion() {
        return version;
    }

    @Contract(pure = true)
    public @NotNull String getPublisherId() {
        return publisherId;
    }
}
