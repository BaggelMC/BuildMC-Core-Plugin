package net.mathias2246.buildmc.api.event.team;

import net.mathias2246.buildmc.api.team.Team;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Thrown when an entities {@link Team} is about to change.
 * <p>
 *     Can be cancelled team to not apply the new {@link Team} to the entity who is involved in this event.
 * </p>
 * <p>
 *     A team of the value {@code null} counts as no team.
 *     E.g. setting the new team to {@code null} will remove the player from his team.
 * </p>
 * **/
public class EntityChangeTeamEvent extends EntityTeamEvent implements Cancellable {

    private static final @NotNull HandlerList HANDLER_LIST = new HandlerList();

    private boolean cancelled = false;

    private @Nullable Team newTeam;

    protected EntityChangeTeamEvent(@NotNull Entity entity, @Nullable Team team, @Nullable Team newTeam) {
        super(entity, team);
        this.newTeam = newTeam;
    }

    public void setNewTeam(@Nullable Team newTeam) {
        this.newTeam = newTeam;
    }

    public @Nullable Team getNewTeam() {
        return newTeam;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static @NotNull HandlerList getHandlerList() { return HANDLER_LIST; }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }
}
