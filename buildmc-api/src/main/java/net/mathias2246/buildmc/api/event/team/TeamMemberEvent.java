package net.mathias2246.buildmc.api.event.team;

import net.mathias2246.buildmc.api.team.Team;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/** The base class for all {@link TeamEvent}s where a certain team member is involved in.
 *
 * @see TeamEvent
 * **/
public abstract class TeamMemberEvent extends TeamEvent {

    private final @NotNull UUID memberId;

    /** Base constructor for {@link TeamMemberEvent}s.
     *
     * @param memberId The {@link UUID} of the member involved in this event.
     * @param team The {@link Team} that is involved in this event
     *
     * @throws IllegalStateException if the given {@link Team} is not registered in a team manager.
     * **/
    public TeamMemberEvent(@NotNull UUID memberId, @NotNull Team team) {
        super(team);
        this.memberId = memberId;
    }

    /** Returns the {@link UUID} of the member involved in this event.
     *
     * @return the {@link UUID} of the member involved in this event.
     * **/
    public @NotNull UUID getMemberId() {
        return memberId;
    }
}
