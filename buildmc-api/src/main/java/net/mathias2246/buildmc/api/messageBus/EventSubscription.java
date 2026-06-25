package net.mathias2246.buildmc.api.messageBus;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class EventSubscription implements Keyed {
    private final NamespacedKey key;
    private final NamespacedKey channelId;
    private final Set<SemVer> acceptedVersions;
    private final String subscriberId;
    private final EventHandler handler;

    public EventSubscription(@NotNull NamespacedKey key, @NotNull NamespacedKey channelId, @NotNull Set<SemVer> acceptedVersions, @NotNull String subscriberId, @NotNull EventHandler handler) {
        this.key = key;
        this.channelId = channelId;
        this.acceptedVersions = acceptedVersions;
        this.subscriberId = subscriberId;
        this.handler = handler;
    }

    @Override
    public @NotNull NamespacedKey getKey() { return key; }

    public @NotNull NamespacedKey getChannelId() { return channelId; }

    public @NotNull Set<SemVer> getAcceptedVersions() { return acceptedVersions; }

    public @NotNull String getSubscriberId() { return subscriberId; }

    public @NotNull EventHandler getHandler() { return handler; }
}