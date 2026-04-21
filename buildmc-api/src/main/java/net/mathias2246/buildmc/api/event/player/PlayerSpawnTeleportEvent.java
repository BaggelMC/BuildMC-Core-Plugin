package net.mathias2246.buildmc.api.event.player;

import net.mathias2246.buildmc.api.event.CustomPlayerEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/** Called when a player uses the '/spawn' command to teleport to spawn.
 * <p>If this event was cancelled the player won't be teleported.</p>
 * **/
public class PlayerSpawnTeleportEvent extends CustomPlayerEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancelled = false;

    private @NotNull Location to;

    /** The default constructor for creating {@link PlayerSpawnTeleportEvent}.
     *
     * @param who The player who gets teleported
     * @param to The location the player gets teleported to.
     * **/
    public PlayerSpawnTeleportEvent(@NotNull Player who, @NotNull Location to) {
        super(who);
        this.to = to;
    }

    /**
     * Sets the {@link Location} to where the player is teleported after waiting.
     *
     * @param to The new teleport target location
     * **/
    public void setTo(@NotNull Location to) {
        this.to = to;
    }

    /**
     * Gets the {@link Location} to where the player is teleported after waiting.
     *
     * @return The teleport target location
     * **/
    public @NotNull Location getTo() {
        return to;
    }

    /** Gets the cancellation state of this event.
     * <p>
     *     If the event was cancelled, the player will not be teleported to the target location.
     * </p>
     * @return If this event was cancelled.
     * **/
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /** Sets the cancellation state of this event.
     * <p>
     *     If the event was cancelled, the player will not be teleported to the target location.
     * </p>
     * @param cancelled if this event should be cancelled or not.
     * **/
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
