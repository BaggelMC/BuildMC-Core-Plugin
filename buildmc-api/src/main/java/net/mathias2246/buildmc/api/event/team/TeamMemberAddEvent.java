package net.mathias2246.buildmc.api.event.team;

import net.mathias2246.buildmc.api.team.Team;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/** An event that is fired before a member is added to a {@link Team}.
 * <p>
 *     This event can be optionally marked as cancellable.
 * </p>
 *
 * */
public class TeamMemberAddEvent extends TeamMemberEvent implements Cancellable {

    private static final @NotNull HandlerList HANDLERS = new HandlerList();

    private boolean cancelled = false;

    private final boolean cancellable;

    /** The base constructor for {@link TeamMemberAddEvent}s.
     *
     * @param memberId The {@link UUID} of the member to add.
     * @param team The registered {@link Team} the member is added to.
     * @param cancellable If true, this event can be cancelled.
     * **/
    public TeamMemberAddEvent(@NotNull UUID memberId, @NotNull Team team, boolean cancellable) {
        super(memberId, team);
        this.cancellable = cancellable;
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
        return cancellable && cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancellable && cancel;
    }
}
