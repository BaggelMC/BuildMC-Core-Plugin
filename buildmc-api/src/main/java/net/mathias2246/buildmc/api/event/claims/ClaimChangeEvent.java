package net.mathias2246.buildmc.api.event.claims;

import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.event.CustomEvent;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a claim setting was changed.
 * This event is fired synchronously on the main server thread.
 * Plugins can attach optional metadata to this event via {@link #putMetadata(String, Object)}.
 */
public class ClaimChangeEvent extends CustomEvent {

    public enum ClaimSetting {
        WHITELIST,
        PROTECTION
    }

    private static final @NotNull HandlerList HANDLERS = new HandlerList();

    private final @NotNull Claim claim;

    private final @NotNull ClaimSetting setting;

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
