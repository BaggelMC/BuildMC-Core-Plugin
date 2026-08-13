package net.mathias2246.buildmc.api.event.team;

import net.mathias2246.buildmc.api.event.CustomEvent;
import net.mathias2246.buildmc.api.team.Team;
import net.mathias2246.buildmc.api.team.TeamManager;
import org.jetbrains.annotations.NotNull;

/** The base class for all {@link org.bukkit.event.Event}s where {@link Team}s are involved in.
 * **/
public abstract class TeamEvent extends CustomEvent {
    private final @NotNull Team team;

    private final @NotNull TeamManager teamManager;

    /** Base constructor for {@link TeamEvent}s.

     * @param team The {@link Team} which is involved in this event
     *
     * @throws IllegalStateException if the given {@link Team} is not registered in a {@link TeamManager}.
     * **/
    public TeamEvent(@NotNull Team team) {
        this.team = team;
        if (team.getTeamManager() == null) {
            throw new IllegalStateException("TeamManager of team in event is null. All subclasses of TeamEvent require a registered team.");
        }
        this.teamManager = team.getTeamManager();
    }

    /** Returns the {@link Team} involved in this event.
     *
     * @return the {@link Team} involved in this event.
     * **/
    public @NotNull Team getTeam() {
        return team;
    }

    /** Returns the {@link TeamManager} the involved team is registered in.
     *
     * @return the {@link TeamManager} the involved team is registered in.
     * **/
    public @NotNull TeamManager getTeamManager() {
        return teamManager;
    }
}
