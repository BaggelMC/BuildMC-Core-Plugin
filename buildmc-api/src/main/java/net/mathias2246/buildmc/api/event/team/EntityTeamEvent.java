package net.mathias2246.buildmc.api.event.team;

import net.mathias2246.buildmc.api.event.CustomEntityEvent;
import net.mathias2246.buildmc.api.team.Team;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Represents a team-related event. **/
public class EntityTeamEvent extends CustomEntityEvent {

    private static final @NotNull HandlerList HANDLER_LIST = new HandlerList();

    private final @Nullable Team team;

    /** Base constructor for {@link EntityTeamEvent}s.
     *
     * @param entity The {@link Entity} who is involved in this event
     * @param team The {@link Team} of the entity who is involved
     * **/
    protected EntityTeamEvent(@NotNull Entity entity, @Nullable Team team) {
        super(entity);
        this.team = team;
    }

    /** Returns the {@link Team} of the entity involved in this event.
     *
     * @return The team of the entity who is involved in this event
     * **/
    public @Nullable Team getTeam() {
        return team;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static @NotNull HandlerList getHandlerList() { return HANDLER_LIST; }

}
