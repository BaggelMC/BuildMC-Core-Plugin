package net.mathias2246.buildmc.api.event.claims;

import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.event.EventMetadata;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Called when a claim setting was changed.
 * This event is fired synchronously on the main server thread.
 * Plugins can attach optional metadata to this event via {@link #putMetadata(String, Object)}.
 */
public class ClaimChangeEvent extends Event implements EventMetadata {

    public enum ClaimSetting {
        WHITELIST,
        PROTECTION
    }

    private static final @NotNull HandlerList HANDLERS = new HandlerList();

    private final @NotNull Claim claim;

    private final @NotNull ClaimSetting setting;

    /** Optional metadata map for plugins to store additional information. */
    private final Map<String, Object> metadata = new HashMap<>();

    /**
     * Constructs a new {@link ClaimChangeEvent}.
     *
     * @param claim the claim that was changed
     * @param setting what setting was changed
     */
    public ClaimChangeEvent(@NotNull Claim claim, @NotNull ClaimSetting setting) {
        this.claim = claim;
        this.setting = setting;
    }

    /**
     * Gets the claim that was changed.
     *
     * @return The claim that was changed
     */
    public @NotNull Claim getClaim() {
        return claim;
    }

    public @NotNull ClaimSetting getSetting() {
        return setting;
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

}
