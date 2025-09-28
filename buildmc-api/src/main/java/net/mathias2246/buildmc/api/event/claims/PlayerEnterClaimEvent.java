package net.mathias2246.buildmc.api.event.claims;

import net.mathias2246.buildmc.api.event.EventMetadata;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Called when a {@link Player} moves from one claim into another.
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
public class PlayerEnterClaimEvent extends PlayerEvent implements EventMetadata {

    private static final @NotNull HandlerList HANDLERS = new HandlerList();

    private final @Nullable Long fromClaim;
    private final @Nullable Long toClaim;

    /** Optional metadata map for plugins to store additional information. */
    private final Map<String, Object> metadata = new HashMap<>();

    /**
     * Constructs a new {@link PlayerEnterClaimEvent}.
     *
     * @param who         the player entering the claim (never {@code null})
     * @param fromClaimId the ID of the claim the player is leaving, or {@code null} if none
     * @param toClaimId   the ID of the claim the player is entering, or {@code null} if none
     */
    public PlayerEnterClaimEvent(@NotNull Player who, @Nullable Long fromClaimId, @Nullable Long toClaimId) {
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
