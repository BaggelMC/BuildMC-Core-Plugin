package net.mathias2246.buildmc.api.messageBus;

import com.google.gson.JsonObject;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public interface EventBus {

    /**
     * Registers a channel that this plugin intends to publish events on.
     * Must be called during {@link net.mathias2246.buildmc.api.event.lifecycle.BuildMcRegistryEvent}.
     */
    void registerChannel(@NotNull Plugin plugin, @NotNull EventChannel channel);

    /**
     * Registers a subscription to a channel.
     * Must be called during {@link net.mathias2246.buildmc.api.event.lifecycle.BuildMcRegistryEvent}.
     */
    void registerSubscription(@NotNull Plugin plugin, @NotNull EventSubscription subscription);

    /**
     * Publishes an event to all compatible subscribers of the given channel.
     * Can be called at any point after the server has fully started.
     */
    void publish(@NotNull NamespacedKey channelId, @NotNull JsonObject payload);
}