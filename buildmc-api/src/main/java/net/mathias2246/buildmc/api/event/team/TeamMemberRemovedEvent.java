package net.mathias2246.buildmc.api.event.team;

import net.mathias2246.buildmc.api.team.Team;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/** An event that fires after a member is removed from a team. **/
public class TeamMemberRemovedEvent extends TeamMemberEvent{

    private static final @NotNull HandlerList HANDLERS = new HandlerList();

    public TeamMemberRemovedEvent(@NotNull UUID memberId, @NotNull Team team) {
        super(memberId, team);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
