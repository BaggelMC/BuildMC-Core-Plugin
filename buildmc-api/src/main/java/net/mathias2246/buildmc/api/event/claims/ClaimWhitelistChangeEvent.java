package net.mathias2246.buildmc.api.event.claims;

import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.event.CustomEvent;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when the whitelist of a {@link Claim} is about to change.
 * <p>
 * This event is fired <strong>before</strong> the whitelist is actually updated.
 * It is cancellable, allowing other plugins to prevent the whitelist change
 * from occurring.
 * </p>
 * <p>
 * This event is called when a plugin, or player adds or removes a member from the whitelist of a claim.
 * </p>
 * <p>
 * If this event is cancelled, the whitelist will remain unchanged.
 * </p>
 */
public class ClaimWhitelistChangeEvent extends CustomEvent implements Cancellable {

    /** Defines which action was applied on the whitelist. **/
    public enum ChangeAction {
        ADDED,
        REMOVED
    }

    private boolean isCancelled;

    private static final @NotNull HandlerList HANDLERS = new HandlerList();

    private final @NotNull Claim claim;

    private final @NotNull OfflinePlayer whitelistedPlayer;

    private final @NotNull ChangeAction action;

    private final long timestamp = System.currentTimeMillis();

    /** The default constructor for creating this event.
     *
     * @param claim The claim where the whitelist is about to be changed.
     * @param whitelistedPlayer The player that will be added or removed from the whitelist depending on the action.
     * @param action The action that will be used on the player in the whitelist
     * **/
    public ClaimWhitelistChangeEvent(@NotNull Claim claim, @NotNull OfflinePlayer whitelistedPlayer, @NotNull ChangeAction action) {
        this.claim = claim;
        this.whitelistedPlayer = whitelistedPlayer;
        this.action = action;
    }

    /** Gets the kind of action that will be used on the player in the whitelist. **/
    public @NotNull ChangeAction getAction() {
        return action;
    }

    /** Gets the player that is about to be added or removed from the whitelist depending on the action.
     * <p>
     * A player must not be online to be added or removed from the whitelist.
     * </p>
     *
     * **/
    public @NotNull OfflinePlayer getWhitelistedPlayer() {
        return whitelistedPlayer;
    }

    /** Gets the claim where the whitelist is about to be changed. **/
    public @NotNull Claim getClaim() {
        return claim;
    }

    /** Gets when the change occurred. **/
    public long getTimestamp() {
        return timestamp;
    }

    /** If the action of this event is {@link ChangeAction#ADDED}.
     *
     * @see ClaimWhitelistChangeEvent#getAction()
     * **/
    public boolean isAddedAction() {
        return action == ChangeAction.ADDED;
    }

    /** If the action of this event is {@link ChangeAction#REMOVED}.
     *
     * @see ClaimWhitelistChangeEvent#getAction()
     * **/
    public boolean isRemovedAction() {
        return action == ChangeAction.REMOVED;
    }

    /** Gets the cancellation state of this event.
     * <p>
     * If this event is cancelled, the whitelist will remain unchanged.
     * </p>
     * @return If this event is cancelled**/
    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    /** Sets the cancellation state of this event.
     * <p>
     * If this event is cancelled, the whitelist will remain unchanged.
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
