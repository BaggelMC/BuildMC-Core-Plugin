package net.mathias2246.buildmc.api.event.claims;

import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.event.CustomEvent;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ClaimWhitelistChangeEvent extends CustomEvent implements Cancellable {

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

    public ClaimWhitelistChangeEvent(@NotNull Claim claim, @NotNull OfflinePlayer whitelistedPlayer, @NotNull ChangeAction action) {
        this.claim = claim;
        this.whitelistedPlayer = whitelistedPlayer;
        this.action = action;
    }

    public @NotNull ChangeAction getAction() {
        return action;
    }

    public @NotNull OfflinePlayer getWhitelistedPlayer() {
        return whitelistedPlayer;
    }

    public @NotNull Claim getClaim() {
        return claim;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isAddedAction() {
        return action == ChangeAction.ADDED;
    }

    public boolean isRemovedAction() {
        return action == ChangeAction.REMOVED;
    }


    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

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
