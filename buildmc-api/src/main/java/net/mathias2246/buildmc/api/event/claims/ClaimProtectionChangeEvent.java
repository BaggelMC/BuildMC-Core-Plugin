package net.mathias2246.buildmc.api.event.claims;

import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.api.event.CustomEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a {@link Protection} of a {@link Claim} is about to change.
 * <p>
 * This event is fired <strong>before</strong> the protection status is actually updated.
 * It is cancellable, allowing other plugins to prevent the protection status change
 * from occurring.
 * </p>
 * <p>
 * This event is called when a plugin, or player toggles a protection of a claim.
 * </p>
 * <p>
 * If this event is cancelled, the protection status will remain unchanged.
 * </p>
 */
public class ClaimProtectionChangeEvent extends CustomEvent implements Cancellable {

    public enum ActiveState {
        ENABLED,
        DISABLED
    }

    private boolean isCancelled;

    private static final @NotNull HandlerList HANDLERS = new HandlerList();

    private final @NotNull Claim claim;

    private final @NotNull Protection protection;

    private final @NotNull ActiveState newState;

    private final long timestamp = System.currentTimeMillis();

    public ClaimProtectionChangeEvent(@NotNull Claim claim, @NotNull Protection protection, @NotNull ActiveState newState) {
        this.claim = claim;
        this.protection = protection;
        this.newState = newState;
    }

    /** Gets the claim where the protection was toggled on or off. **/
    public @NotNull Claim getClaim() {
        return claim;
    }

    /** Gets the protection that was toggled on or off. **/
    public @NotNull Protection getProtection() {
        return protection;
    }

    /** Gets the new state of the protection after the change. **/
    public @NotNull ActiveState getNewState() {
        return newState;
    }

    /** Gets when the change occurred. **/
    public long getTimestamp() {
        return timestamp;
    }

    /** If the new state of the protection is {@link ActiveState#ENABLED}.
     *
     * @see ClaimProtectionChangeEvent#getNewState()
     * **/
    public boolean isEnabled() {
        return newState == ActiveState.ENABLED;
    }

    /** If the new state of the protection is {@link ActiveState#DISABLED}.
     *
     * @see ClaimProtectionChangeEvent#getNewState()
     * **/
    public boolean isDisabled() {
        return newState == ActiveState.DISABLED;
    }

    /** Gets the cancellation state of this event.
     * <p>
     * If this event is cancelled, the protection status will remain unchanged.
     * </p>
     * @return If this event is cancelled**/
    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    /** Sets the cancellation state of this event.
     * <p>
     * If this event is cancelled, the protection status will remain unchanged.
     * </p>
     * @param b If the event should be cancelled or not**/
    @Override
    public void setCancelled(boolean b) {
        this.isCancelled = b;
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
