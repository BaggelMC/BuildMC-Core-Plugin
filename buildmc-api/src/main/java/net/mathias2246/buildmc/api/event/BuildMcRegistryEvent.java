package net.mathias2246.buildmc.api.event;

import net.mathias2246.buildmc.api.BuildMcAPI;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * The {@code BuildMcRegistryEvent} is fired once during the plugin startup lifecycle
 * to give other plugins a chance to register or modify values in BuildMC’s
 * registries <em>before</em> they are finalized and made immutable.
 * </p>
 *
 * <h2>Why this event exists</h2>
 * <ul>
 *     <li>The BuildMC plugin must load first to expose its API.</li>
 *     <li>Other plugins (extensions) may want to change behaviours of BuildMC before it starts running.</li>
 *     <li>However, by the time BuildMC itself is fully enabled, its registries
 *         would normally be locked, leaving no safe way for extensions to hook in.</li>
 * </ul>
 *
 * <p>
 * This event solves that ordering problem by deferring registry finalization until
 * after <strong>all plugins have been enabled</strong>. Once the server signals that
 * plugin startup is complete, BuildMC fires this event, passing its API instance.
 * Extensions can then safely register or modify values before the registries are frozen.
 * </p>
 *
 * <h2>How to use it</h2>
 *
 * <pre>{@code
 * public class MyExtensionPlugin extends JavaPlugin implements Listener {
 *
 *     @Override
 *     public void onEnable() {
 *         // Register as a listener to receive BuildMcRegistryEvent
 *         Bukkit.getPluginManager().registerEvents(this, this);
 *     }
 *
 *     @EventHandler
 *     public void onRegistry(BuildMcRegistryEvent event) {
 *         BuildMcAPI api = event.getApi();
 *
 *         // Configure the API here...
 *     }
 * }
 * }</pre>
 *
 * <h2>Lifecycle</h2>
 * <ol>
 *     <li>BuildMC loads and initializes its API with empty, mutable registries.</li>
 *     <li>Other plugins enable normally and register listeners for this event.</li>
 *     <li>After all plugins are enabled, BuildMC fires this event once.</li>
 *     <li>Extensions receive the API and make their registrations.</li>
 *     <li>BuildMC finalizes its registries and loads the config, making them immutable for runtime.</li>
 * </ol>
 *
 * @see BuildMcAPI
 */
public class BuildMcRegistryEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final BuildMcAPI api;

    /**
     * Creates a new {@link BuildMcRegistryEvent}.
     *
     * @param api the active {@link BuildMcAPI} instance,
     *            providing access to registries and configuration
     */
    public BuildMcRegistryEvent(@NotNull BuildMcAPI api) {
        this.api = api;
    }

    /**
     * Gets the BuildMC API instance.
     * <p>
     * Extensions should use this API to add or modify registry entries
     * during the registration phase.
     * </p>
     *
     * @return the BuildMC API instance
     */
    @NotNull
    public BuildMcAPI getApi() {
        return api;
    }

    /**
     * Gets the list of handlers for this event.
     * Required by the Bukkit event system.
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
