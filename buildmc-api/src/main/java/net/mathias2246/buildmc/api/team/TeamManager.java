package net.mathias2246.buildmc.api.team;

import com.google.common.collect.ImmutableMap;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@ApiStatus.NonExtendable
public interface TeamManager {

    /** Tries to find a registered team with the given string identifier.
     *
     * @param teamIdentifier the identifier of the team instance to get
     * @return The {@link Team} with the given identifier, or {@code null} if not found
     * **/
    @Nullable Team getTeam(@NotNull String teamIdentifier);

    /** Registers a team so that it can be found and optionally persist in storage.
     *
     * @param team The team instance to register
     * @param persist If this registered team should persist in storage.
     *
     * @return {@code true} if the team was registered successfully.
     * **/
    boolean registerTeam(@NotNull Team team, boolean persist);

    /** Checks if the given team is registered in this manager by its name.
     *
     * @param teamIdentifier The unique name of the team
     *
     * @return {@code true} if the team is registered in this manager.
     * **/
    @Contract(pure = true)
    boolean isRegisteredTeam(@NotNull String teamIdentifier);

    /** Checks if the given team is registered in this manager.
     *
     * @param team the team to check
     *
     * @return {@code true} if the team is registered in this manager.
     * **/
    @Contract(pure = true)
    boolean isRegisteredTeam(@NotNull Team team);

    /** Returns if the team with the given identifier is persistent or not.
     * This will be {@code null} when not registered.
     *
     * @param teamIdentifier The unique name of the team to check
     *
     * @return {@code true} if the team is marked as persistent, or {@code null} if not registered.
     * **/
    @Contract(pure = true)
    @Nullable Boolean isPersistent(@NotNull String teamIdentifier);

    /** Returns if the given team is persistent or not.
     * This will be {@code null} when not registered.
     *
     * @param team The team to check
     *
     * @return {@code true} if the team is marked as persistent, or {@code null} if not registered.
     * **/
    @Contract(pure = true)
    @Nullable Boolean isPersistent(@NotNull Team team);

    /** Unregisters a team from this manager.
     * This will fire a {@link net.mathias2246.buildmc.api.event.team.TeamUnregisterEvent}.
     *
     * @param teamIdentifier The unique name of the team to remove
     * **/
    void unregisterTeam(@NotNull String teamIdentifier);

    /** Unregisters a team from this manager.
     * This will fire a {@link net.mathias2246.buildmc.api.event.team.TeamUnregisterEvent}.
     *
     * @param team The team to remove
     * **/
    void unregisterTeam(@NotNull Team team);

    /** Tries to return the team where the given entity is on.
     *
     * @param entity The entity to check
     * @return The {@link Team} the entity is in, or {@code null} if the entity is in no team.
     * **/
    @Nullable Team getTeamFromMember(@NotNull Entity entity);

    /** Tries to return the team where the given {@link UUID} is on.
     *
     * @param uuid The {@link UUID} to check
     * @return The {@link Team} the {@link UUID} is on, or {@code null} if the {@link UUID} is on no team.
     * **/
    @Nullable Team getTeamFromMember(@NotNull UUID uuid);

    /** Checks if the given {@link UUID} is a member on any registered team. **/
    @Contract(pure = true)
    boolean isMember(@NotNull UUID uuid);

    /** Checks if the given {@link Entity} is a member on any registered team. **/
    @Contract(pure = true)
    boolean isMember(@NotNull Entity entity);

    /** Returns an immutable collection of teams.
     *
     * @return a {@link ImmutableMap} of teams that might contain no entries.
     * **/
    @Contract(pure = true)
    @NotNull ImmutableMap<String, Team> getTeams();

    /** Returns a Stream representation of all loaded teams paired with their unique key.
     *
     * @return Stream containing all loaded teams and their keys
     * **/
    @NotNull Stream<Map.Entry<String, Team>> teams();

    /** Tries to make a member join the given team. **/
    boolean joinTeam(@NotNull UUID uuid, @NotNull Team team);

    /** Tries to make a member join the given team. **/
    boolean joinTeam(@NotNull Entity entity, @NotNull Team team);

    /** Makes the given member leave the given team. **/
    boolean leaveTeam(@NotNull UUID uuid, @NotNull Team team);

    /** Makes the given member leave the given team. **/
    boolean leaveTeam(@NotNull Entity entity, @NotNull Team team);

}
