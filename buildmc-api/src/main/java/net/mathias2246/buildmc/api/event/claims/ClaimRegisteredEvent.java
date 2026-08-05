package net.mathias2246.buildmc.api.event.claims;

import com.google.common.collect.ImmutableList;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.event.CustomEvent;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Called after one or more claims have been created and persisted to the database.
 * <p>
 * This event is fired synchronously on the main server thread, <b>after</b> the
 * claims have been persisted. Because the claims have already been persisted,
 * {@link Claim#getId()} is guaranteed to return a non-{@code null} value for
 * every claim in this event.
 * <p>
 * This event is not cancellable, as the claims have already been created by
 * the time it is fired. To prevent claims from being created, cancel
 * {@link ClaimCreateEvent} instead.
 * <p>
 *
 * @see ClaimCreateEvent
 */
public class ClaimRegisteredEvent extends CustomEvent {
    private static final @NotNull HandlerList HANDLERS = new HandlerList();

    private final @NotNull List<@NotNull Claim> claims;

    /**
     * Constructs a new {@link ClaimRegisteredEvent} for multiple claims.
     *
     * @param claims the claims that have just been created. Note that
     *                {@link Claim#getId()} is guaranteed to be non-{@code null}
     *                for each of these, as they have already been persisted.
     */
    public ClaimRegisteredEvent(@NotNull List<@NotNull Claim> claims) {
        this.claims = List.copyOf(claims);
    }

    /**
     * Constructs a new {@link ClaimRegisteredEvent} for a claim.
     *
     * @param claim the claim that has just been created. Note that
     *              {@link Claim#getId()} is guaranteed to be non-{@code null},
     *              as it has already been persisted.
     */
    public ClaimRegisteredEvent(@NotNull Claim claim) {
        this.claims = List.of(claim);
    }

    /**
     * Gets an immutable list of claims that were created.
     * <p>
     * As these claims have already been persisted to the database,
     * {@link Claim#getId()} is guaranteed to return a non-{@code null} value
     * for each of them.
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
     * As with {@link #getClaims()}, the returned claim has already been persisted,
     * so {@link Claim#getId()} is guaranteed to be non-{@code null}.
     *
     * @return the first claim in the list
     */
    public @NotNull Claim getClaim() {
        return claims.getFirst();
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