package net.mathias2246.buildmc.api.event.claims;

import net.mathias2246.buildmc.api.event.CustomPlayerEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a {@link Player} leaves a claim and enters another area.
 * <p>
 * Both the "from" and "to" claim IDs may be {@code null}, and there is no guarantee
 * that the claim IDs correspond to an existing claim at the time of the event.
 * <ul>
 *   <li>{@code fromClaim} may be {@code null} if the player was not previously inside a claim.</li>
 *   <li>{@code toClaim} may be {@code null} if the player is leaving a claim and entering unclaimed land.</li>
 * </ul>
 * <p>
 * This event is fired synchronously on the main server thread.
 * <br><br>
 * Plugins can attach optional metadata to this event via {@link #putMetadata(String, Object)}.
 */
public class PlayerLeaveClaimEvent extends CustomPlayerEvent {

    private static final @NotNull HandlerList HANDLERS = new HandlerList();

    private final @Nullable Long fromClaim;
    private final @Nullable Long toClaim;

    /**
     * Constructs a new {@link PlayerLeaveClaimEvent}.
     *
     * @param who         the player leaving the claim (never {@code null})
     * @param fromClaimId the ID of the claim the player is leaving, or {@code null} if none
     * @param toClaimId   the ID of the claim the player is entering, or {@code null} if unclaimed land
     */
    public PlayerLeaveClaimEvent(@NotNull Player who, @Nullable Long fromClaimId, @Nullable Long toClaimId) {
        super(who);
        this.fromClaim = fromClaimId;
        this.toClaim = toClaimId;
    }

    /**
     * Gets the ID of the claim the player is leaving.
     *
     * @return the ID of the previous claim, or {@code null} if the player was not in a claim
     * @implNote The claim may not exist at the time of this event.
     */
    public @Nullable Long getFromClaim() {
        return fromClaim;
    }

    /**
     * Gets the ID of the claim the player is entering.
     *
     * @return the ID of the new claim, or {@code null} if the player is entering unclaimed land
     * @implNote The claim may not exist at the time of this event.
     */
    public @Nullable Long getToClaim() {
        return toClaim;
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
