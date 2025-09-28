package net.mathias2246.buildmc.api.event.claims;

import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.event.EventMetadata;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Called when a claim was created.
 * This event is fired synchronously on the main server thread.
 * Plugins can attach optional metadata to this event via {@link #putMetadata(String, Object)}.
 */
public class ClaimCreateEvent extends Event implements Cancellable, EventMetadata {

    private static final @NotNull HandlerList HANDLERS = new HandlerList();

    private final @NotNull Claim claim;

    /** Optional metadata map for plugins to store additional information. */
    private final Map<String, Object> metadata = new HashMap<>();

    /**
     * Constructs a new {@link ClaimCreateEvent}.
     *
     * @param claim the claim that was created
     */
    public ClaimCreateEvent(@NotNull Claim claim) {
        this.claim = claim;
    }

    /**
     * Gets the claim that was created.
     *
     * @return The claim that was created
     */
    public @NotNull Claim getClaim() {
        return claim;
    }

    @NotNull
    public Map<String, Object> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    public void putMetadata(@NotNull String key, @NotNull Object value) {
        metadata.put(key, value);
    }

    public void removeMetadata(@NotNull String key) {
        metadata.remove(key);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Gets the static list of handlers for this event type.
     * <p>
     * Required by the Bukkit event system for registration.
     *
     * @return the handler list
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    private boolean isCancelled = false;

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        isCancelled = b;
    }
}
