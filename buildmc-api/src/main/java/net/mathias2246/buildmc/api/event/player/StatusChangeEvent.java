package net.mathias2246.buildmc.api.event.player;

import net.mathias2246.buildmc.api.event.CustomPlayerEvent;
import net.mathias2246.buildmc.api.status.StatusInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**Called when a players status is about to change.
 * <p>
 *     You can cancel this event if you want to prevent the player from changing his status.
 * </p>
 * */
public class StatusChangeEvent extends CustomPlayerEvent implements Cancellable {

    private static final @NotNull HandlerList HANDLERS = new HandlerList();

    /**@return The {@link StatusInstance} the player had before changing*/
    public @Nullable StatusInstance getOldStatus() {
        return oldStatus;
    }

    private final @Nullable StatusInstance oldStatus;

    /**@return The {@link StatusInstance} the player will get after this event has successfully executed.*/
    public @Nullable StatusInstance getNewStatus() {
        return newStatus;
    }

    /**@param newStatus The status the player gets after this event has successfully executed
     *      <p>
     *          If set to {@code null}, the players current status will be removed.
     *      </p>*/
    public void setNewStatus(@Nullable StatusInstance newStatus) {
        this.newStatus = newStatus;
    }

    private @Nullable StatusInstance newStatus;

    private boolean isCancelled = false;

    /**The default constructor
     * @param newStatus The {@link StatusInstance} the player gets after this event was successfully executed.<p>If {@code null} the current status of the player will be removed.</p>
     * @param oldStatus The {@link StatusInstance} the player had before trying to change his status.<p>Is {@code null} if the players status was not set.</p>
     * @param player  the player associated with this event (must not be null)*/
    public StatusChangeEvent(@NotNull Player player, @Nullable StatusInstance oldStatus, @Nullable StatusInstance newStatus) {
        super(player);
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        isCancelled = b;
    }
}
