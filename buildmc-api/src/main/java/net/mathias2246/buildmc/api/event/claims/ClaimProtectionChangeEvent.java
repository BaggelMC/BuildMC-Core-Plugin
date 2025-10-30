package net.mathias2246.buildmc.api.event.claims;

import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.api.event.CustomEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

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

    private final @NotNull CommandSender actor;

    private final long timestamp = System.currentTimeMillis();

    public ClaimProtectionChangeEvent(@NotNull Claim claim, @NotNull Protection protection, @NotNull ActiveState newState, @NotNull CommandSender actor) {
        this.claim = claim;
        this.protection = protection;
        this.newState = newState;
        this.actor = actor;
    }

    public @NotNull Claim getClaim() {
        return claim;
    }

    public @NotNull Protection getProtection() {
        return protection;
    }

    public @NotNull ActiveState getNewState() {
        return newState;
    }

    private final @NotNull CommandSender getActor() {
        return actor;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isEnabled() {
        return newState == ActiveState.ENABLED;
    }

    public boolean isDisabled() {
        return newState == ActiveState.DISABLED;
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
