package net.mathias2246.buildmc.api.event.team;

import net.mathias2246.buildmc.api.team.Team;
import net.mathias2246.buildmc.api.team.TeamManager;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/** An event that is fired before a {@link Team} is unregistered from a {@link TeamManager}. */
public class TeamUnregisterEvent extends TeamEvent {
    private static final @NotNull HandlerList HANDLERS = new HandlerList();

    /**
     * @throws IllegalStateException if the given {@link Team} is not registered in a {@link TeamManager}.
     * */
    public TeamUnregisterEvent(@NotNull Team team) {
        super(team);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
