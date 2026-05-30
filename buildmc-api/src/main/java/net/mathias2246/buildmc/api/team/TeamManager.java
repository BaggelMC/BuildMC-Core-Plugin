package net.mathias2246.buildmc.api.team;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.NonExtendable
public interface TeamManager {

    /** Tries to find a registered team with the given string identifier.
     *
     * @param teamIdentifier the identifier of the team instance to get
     * @return The {@link Team} with the given identifier, or {@code null} if not found
     * **/
    public abstract @Nullable Team getTeam(@NotNull String teamIdentifier);

    /** Registers a team so that it can be found and persist in storage.
     *
     * @param team The team instance to register
     * **/
    public abstract void registerTeam(@NotNull Team team);

    /** Tries to return the team where the given entity is a member of.
     *
     * @param entity The entity to check
     * @return The {@link Team} the entity is in, or {@code null} if the entity is in no team.
     * **/
    public abstract @Nullable Team getTeamFromEntity(@NotNull Entity entity);
}
