package net.mathias2246.buildmc.api.event.claims;

import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.event.CustomEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Called when one or more claims are created.
 * <p>
 * This event is fired synchronously on the main server thread.
 * If the event is canceled, <b>none</b> of the claims should be created.
 * <p>
 * Plugins may attach optional metadata to this event via
 * {@link #putMetadata(String, Object)}.
 */
public class ClaimCreateEvent extends CustomEvent implements Cancellable {

    private static final @NotNull HandlerList HANDLERS = new HandlerList();

    private final @NotNull List<@NotNull Claim> claims;

    private boolean cancelled = false;

    /**
     * Constructs a new {@link ClaimCreateEvent} for multiple claims.
     *
     * @param claims the claims that are about to be created
     */
    public ClaimCreateEvent(@NotNull List<@NotNull Claim> claims) {
        this.claims = List.copyOf(claims);
    }

    /**
     * Constructs a new {@link ClaimCreateEvent} for a claim.
     *
     * @param claim the claim that is about to be created
     */
    public ClaimCreateEvent(@NotNull Claim claim) {
        this.claims = List.of(claim);
    }

    /**
     * Gets an immutable list of claims being created.
     *
     * @return all claims included in this creation operation
     */
    public @NotNull List<@NotNull Claim> getClaims() {
        return Collections.unmodifiableList(claims);
    }

    /**
     * Convenience method for single-claim operations.
     * <p>
     * If multiple claims are present, this returns the first one.
     * Plugins that care about bulk operations should use {@link #getClaims()}.
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
