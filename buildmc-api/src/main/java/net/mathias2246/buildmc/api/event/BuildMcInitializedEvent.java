package net.mathias2246.buildmc.api.event;

import net.mathias2246.buildmc.api.BuildMcAPI;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when BuildMC has finished its initialization and is ready for
 * external plugins to interact with. This is the safe point to access
 * BuildMC's API, register claims, modify configs, or hook into extension points.
 */
public class BuildMcInitializedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * The BuildMC API instance.
     */
    private final BuildMcAPI api;

    /**
     * Constructs a new BuildMcInitializedEvent.
     *
     * @param api the usable BuildMC API instance
     */
    public BuildMcInitializedEvent(@NotNull BuildMcAPI api) {
        this.api = api;
    }

    /**
     * Gets the BuildMC API instance.
     *
     * @return the BuildMC API instance
     */
    @NotNull
    public BuildMcAPI getApi() {
        return api;
    }

    /**
     * Gets the list of handlers for this event.
     *
     * @return the handler list
     */
    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Gets the static list of handlers for this event type.
     * Required by Bukkit for event registration.
     *
     * @return the handler list
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
