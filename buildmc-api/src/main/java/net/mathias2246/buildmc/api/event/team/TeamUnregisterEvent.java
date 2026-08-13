package net.mathias2246.buildmc.api.event.team;

import net.mathias2246.buildmc.api.team.Team;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TeamUnregisterEvent extends TeamEvent {
    private static final @NotNull HandlerList HANDLERS = new HandlerList();

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
