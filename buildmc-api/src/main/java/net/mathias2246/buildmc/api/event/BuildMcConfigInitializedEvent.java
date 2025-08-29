package net.mathias2246.buildmc.api.event;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when BuildMC's configuration file has been initialized, but before
 * it is applied or read by the plugin. This allows external plugins to safely
 * modify, add, or override config values before BuildMC uses them.
 */
public class BuildMcConfigInitializedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * The initialized configuration file. Addons can modify this configuration
     * safely at this point.
     */
    private final FileConfiguration config;

    /**
     * Constructs a new BuildMcConfigInitializedEvent.
     *
     * @param config the FileConfiguration instance that can be modified
     */
    public BuildMcConfigInitializedEvent(@NotNull FileConfiguration config) {
        this.config = config;
    }

    /**
     * Gets the FileConfiguration instance for BuildMC's config.
     * Addons can safely modify it before it is applied.
     *
     * @return the FileConfiguration instance
     */
    @NotNull
    public FileConfiguration getConfig() {
        return config;
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
