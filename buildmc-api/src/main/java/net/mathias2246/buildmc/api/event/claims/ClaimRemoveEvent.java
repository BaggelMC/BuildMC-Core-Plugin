package net.mathias2246.buildmc.api.event.claims;

import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.event.CustomEvent;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Called when one or more claims were removed.
 * <p>
 * This event is fired synchronously on the main server thread.
 * Plugins can attach optional metadata to this event via
 * {@link #putMetadata(String, Object)}.
 */
public class ClaimRemoveEvent extends CustomEvent {

    private static final @NotNull HandlerList HANDLERS = new HandlerList();

    private final @NotNull List<@NotNull Claim> claims;

    /**
     * Constructs a new {@link ClaimRemoveEvent} for multiple claims.
     *
     * @param claims the claims that were removed
     */
    public ClaimRemoveEvent(@NotNull List<@NotNull Claim> claims) {
        this.claims = List.copyOf(claims);
    }

    /**
     * Constructs a new {@link ClaimRemoveEvent} for a single claim.
     *
     * @param claim the claim that was removed
     */
    public ClaimRemoveEvent(@NotNull Claim claim) {
        this.claims = List.of(claim);
    }

    /**
     * Gets an immutable list of claims that were removed.
     *
     * @return all claims included in this removal operation
     */
    public @NotNull List<@NotNull Claim> getClaims() {
        return Collections.unmodifiableList(claims);
    }

    /**
     * Convenience method for single-claim operations.
     * <p>
     * If multiple claims are present, this returns the first one.
     * Plugins that care about bulk operations (BulkMC-Core xD) should use {@link #getClaims()}.
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
