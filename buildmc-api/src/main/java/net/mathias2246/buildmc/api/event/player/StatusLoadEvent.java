package net.mathias2246.buildmc.api.event.player;

import net.mathias2246.buildmc.api.event.CustomEvent;
import net.mathias2246.buildmc.api.status.StatusInstance;
import net.mathias2246.buildmc.util.registry.BaseRegistry;
import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event fired during the loading (and reloading) phase of statuses.
 * <p>
 * This event allows plugins or internal systems to register or unregister
 * {@link StatusInstance} objects before the status registry is finalized.
 * </p>
 */
public class StatusLoadEvent extends CustomEvent {

    private static final @NotNull HandlerList HANDLERS = new HandlerList();

    private final BaseRegistry<StatusInstance> statusRegistry;

    /**
     * Creates a new {@code StatusLoadEvent}.
     *
     * @param statusRegistry the registry that will contain all loaded statuses
     */
    public StatusLoadEvent(@NotNull BaseRegistry<StatusInstance> statusRegistry) {
        this.statusRegistry = statusRegistry;
    }

    /**
     * Returns the registry being populated during this load event.
     *
     * @return the status registry
     */
    public BaseRegistry<StatusInstance> getStatusRegistry() {
        return statusRegistry;
    }

    /**
     * Adds a status to the registry during the loading phase.
     *
     * @param status the status instance to register
     */
    public void addStatus(StatusInstance status) {
        statusRegistry.addEntry(status);
    }

    /**
     * Removes a previously registered status from the registry during loading.
     *
     * @param statusKey the namespaced key identifying the status to remove
     */
    public void removeStatus(NamespacedKey statusKey) {
        statusRegistry.removeEntry(statusKey);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Required static method for Bukkit event registration.
     *
     * @return the handler list for this event type
     */
    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
