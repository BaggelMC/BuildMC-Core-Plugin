package net.mathias2246.buildmc.api.event.claims;

import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.event.CustomEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when the owner ID of a {@link Claim} is about to change.
 * <p>
 * This event is fired <strong>before</strong> the owner is actually updated.
 * It is cancellable, allowing other plugins to prevent the ownership change
 * from occurring.
 * </p>
 *
 * <p>
 * The owner ID is a string-based identifier whose meaning depends on the
 * {@link net.mathias2246.buildmc.api.claims.ClaimType ClaimType}. It may
 * represent a player UUID (as a string), a team identifier, or a special
 * value such as {@code "server"}.
 * </p>
 *
 * <p>
 * Typical this is only triggered by another plugin changing the owner via the API.
 * </p>
 *
 * <p>
 * If this event is cancelled, the claim owner will remain unchanged.
 * </p>
 */
public class ClaimOwnerChangeEvent extends CustomEvent implements Cancellable {

    private static final @NotNull HandlerList HANDLERS = new HandlerList();

    private final @NotNull Claim claim;
    private final @NotNull String oldOwnerId;
    private final @NotNull String newOwnerId;

    private final long timestamp = System.currentTimeMillis();

    private boolean cancelled;

    /**
     * Creates a new claim owner change event.
     *
     * @param claim       The claim whose owner is changing
     * @param oldOwnerId  The previous owner ID
     * @param newOwnerId  The new owner ID that will be set if the event is not cancelled
     */
    public ClaimOwnerChangeEvent(@NotNull Claim claim,
                                 @NotNull String oldOwnerId,
                                 @NotNull String newOwnerId) {
        this.claim = claim;
        this.oldOwnerId = oldOwnerId;
        this.newOwnerId = newOwnerId;
    }

    /**
     * Gets the claim whose ownership is being changed.
     * The state did not change to reflect the update yet.
     *
     * @return the affected claim
     */
    public @NotNull Claim getClaim() {
        return claim;
    }

    /**
     * Gets the previous owner ID of the claim.
     *
     * @return the old owner ID
     */
    public @NotNull String getOldOwnerId() {
        return oldOwnerId;
    }

    /**
     * Gets the new owner ID that will be assigned to the claim if the
     * event is not cancelled.
     *
     * @return the new owner ID
     */
    public @NotNull String getNewOwnerId() {
        return newOwnerId;
    }

    /**
     * Gets the timestamp (in milliseconds since epoch) when this event
     * instance was created.
     *
     * @return the event creation timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Checks whether this event has been cancelled.
     *
     * @return {@code true} if the ownership change should be prevented
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets the cancellation state of this event.
     *
     * @param cancel {@code true} to prevent the ownership change, {@code false} to allow it
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    /**
     * Gets the handler list for this event instance.
     *
     * @return the handler list
     */
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
    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
