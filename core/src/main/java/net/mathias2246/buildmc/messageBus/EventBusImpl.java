package net.mathias2246.buildmc.messageBus;

import com.google.gson.JsonObject;
import net.mathias2246.buildmc.api.messageBus.*;
import net.mathias2246.buildmc.util.registry.DeferredRegistry;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Logger;

public class EventBusImpl implements EventBus {

    private final DeferredRegistry<EventChannel> channelRegistry;
    private final DeferredRegistry<EventSubscription> subscriptionRegistry;
    private final Logger logger;

    // Built once at lock time, used on every publish
    private final Map<String, List<EventSubscription>> subscriptionIndex = new HashMap<>();

    public EventBusImpl(
            DeferredRegistry<EventChannel> channelRegistry,
            DeferredRegistry<EventSubscription> subscriptionRegistry,
            Logger logger
    ) {
        this.channelRegistry = channelRegistry;
        this.subscriptionRegistry = subscriptionRegistry;
        this.logger = logger;
    }

    @Override
    public void registerChannel(@NotNull Plugin plugin, @NotNull EventChannel channel) {
        channelRegistry.addEntryOrThrow(plugin, channel);
    }

    @Override
    public void registerSubscription(@NotNull Plugin plugin, @NotNull EventSubscription subscription) {
        subscriptionRegistry.addEntryOrThrow(plugin, subscription);
    }

    /**
     * Called by core after both registries are locked.
     * Validates all contracts and builds the subscription index.
     */
    @ApiStatus.Internal
    public void initialize() {
        validateAndBuildIndex();
    }

    @Override
    public void publish(@NotNull NamespacedKey channelId, @NotNull JsonObject payload) {
        EventChannel channel = channelRegistry.get(channelId);
        if (channel == null) {
            return;
        }

        BusEvent event = new BusEvent(
                channelId.asString(),
                channel.getVersion(),
                channel.getPublisherId(),
                payload,
                System.currentTimeMillis()
        );

        List<EventSubscription> subscribers = subscriptionIndex
                .getOrDefault(channelId.asString(), Collections.emptyList());

        for (EventSubscription sub : subscribers) {
            try {
                sub.getHandler().handle(event);
            } catch (Exception e) {
                logger.severe("[EventBus] Handler in '" + sub.getSubscriberId() +
                        "' threw an exception on channel '" + channelId.asString() + "': " + e.getMessage());
            }
        }
    }

    private void validateAndBuildIndex() {
        for (EventSubscription sub : subscriptionRegistry) {
            EventChannel channel = channelRegistry.get(sub.getChannelId());

            if (channel == null) {
                logger.severe("[EventBus] No publisher found for channel '" +
                        sub.getChannelId().asString() + "' (subscribed by '" + sub.getSubscriberId() + "')");
                continue;
            }

            SemVer published = channel.getVersion();
            boolean anyCompatible = sub.getAcceptedVersions().stream()
                    .anyMatch(published::isCompatibleWith);

            if (!anyCompatible) {
                logger.severe("[EventBus] Version mismatch on channel '" +
                        sub.getChannelId().asString() + "': publisher emits " + published +
                        ", subscriber '" + sub.getSubscriberId() + "' accepts " + sub.getAcceptedVersions());
                continue; // don't index incompatible subscribers
            }

            subscriptionIndex
                    .computeIfAbsent(sub.getChannelId().asString(), k -> new ArrayList<>())
                    .add(sub);
        }
    }
}