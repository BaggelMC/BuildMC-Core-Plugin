package net.mathias2246.buildmc.api.event.claims;

import com.google.common.collect.ImmutableList;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.event.CustomEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Called when one or more claims are created.
 * <p>
 * This event is fired synchronously on the main server thread, <b>before</b> the
 * claims are persisted to the database. Because the claims have not yet been
 * persisted, {@link Claim#getId()} will always return {@code null} for every
 * claim in this event — the ID is assigned by the database layer once the
 * claim is actually saved.
 * <p>
 * If the event is cancelled, <b>none</b> of the claims will be created.
 * <p>
 * Once the claims have been successfully persisted, {@link ClaimRegisteredEvent}
 * is fired with the fully persisted claims (i.e. {@link Claim#getId()} populated).
 * <p>
 *
 * @see ClaimRegisteredEvent
 */
public class ClaimCreateEvent extends CustomEvent implements Cancellable {

    private static final @NotNull HandlerList HANDLERS = new HandlerList();

    private final @NotNull List<@NotNull Claim> claims;

    private boolean cancelled = false;

    /**
     * Constructs a new {@link ClaimCreateEvent} for multiple claims.
     *
     * @param claims the claims that are about to be created. Note that
     *                {@link Claim#getId()} will be {@code null} for each of
     *                these, as they have not yet been persisted. Once
     *                persisted, {@link ClaimRegisteredEvent} will be fired
     *                with the fully populated claims.
     */
    public ClaimCreateEvent(@NotNull List<@NotNull Claim> claims) {
        this.claims = List.copyOf(claims);
    }

    /**
     * Constructs a new {@link ClaimCreateEvent} for a claim.
     *
     * @param claim the claim that is about to be created. Note that
     *              {@link Claim#getId()} will be {@code null}, as it has
     *              not yet been persisted. Once persisted,
     *              {@link ClaimRegisteredEvent} will be fired with the
     *              fully populated claim.
     */
    public ClaimCreateEvent(@NotNull Claim claim) {
        this.claims = List.of(claim);
    }

    /**
     * Gets an immutable list of claims being created.
     * <p>
     * As these claims have not yet been persisted to the database,
     * {@link Claim#getId()} will always return {@code null} for each of them.
     * See {@link ClaimRegisteredEvent} for an equivalent event fired after
     * persistence, where the IDs are populated.
     *
     * @return all claims included in this creation operation
     */
    public @NotNull ImmutableList<@NotNull Claim> getClaims() {
        return ImmutableList.copyOf(claims);
    }

    /**
     * Convenience method for single-claim operations.
     * <p>
     * If multiple claims are present, this returns the first one.
     * Plugins that care about bulk operations (BulkMC-Core xD) should use {@link #getClaims()}.
     * <p>
     * As with {@link #getClaims()}, the returned claim has not yet been persisted,
     * so {@link Claim#getId()} will return {@code null}. See
     * {@link ClaimRegisteredEvent#getClaim()} for the persisted equivalent.
     *
     * @return the first claim in the list
     */
    public @NotNull Claim getClaim() {
        return claims.getFirst();
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Required by the Bukkit event system for registration.
     *
     * @return the static handler list
     */
    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}