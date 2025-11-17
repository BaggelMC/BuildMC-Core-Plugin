package net.mathias2246.buildmc.api.event.player;

import net.mathias2246.buildmc.api.event.CustomPlayerEvent;
import net.mathias2246.buildmc.api.status.StatusInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StatusChangeEvent extends CustomPlayerEvent implements Cancellable {

    private static final @NotNull HandlerList HANDLERS = new HandlerList();

    public @Nullable StatusInstance getOldStatus() {
        return oldStatus;
    }

    private final @Nullable StatusInstance oldStatus;

    public @Nullable StatusInstance getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(@Nullable StatusInstance newStatus) {
        this.newStatus = newStatus;
    }

    private @Nullable StatusInstance newStatus;

    private boolean isCancelled = false;

    public StatusChangeEvent(@NotNull Player player, @Nullable StatusInstance oldStatus, @Nullable StatusInstance newStatus) {
        super(player);
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public @NotNull HandlerList getHandlerList() {
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
