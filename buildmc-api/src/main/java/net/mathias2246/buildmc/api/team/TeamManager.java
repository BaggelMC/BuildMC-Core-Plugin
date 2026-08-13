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
     * **/
    boolean registerTeam(@NotNull Team team, boolean persist);

    @Contract(pure = true)
    boolean isRegisteredTeam(@NotNull String teamIdentifier);

    /** Returns if the team with the given identifier is persistent or not.
     * This will be {@code null} when not registered or unset.
     * **/
    @Contract(pure = true)
    @Nullable Boolean isPersistent(@NotNull String teamIdentifier);

    void unregisterTeam(@NotNull String teamIdentifier);

    void unregisterTeam(@NotNull Team team);

    /** Tries to return the team where the given entity is a member of.
     *
     * @param entity The entity to check
     * @return The {@link Team} the entity is in, or {@code null} if the entity is in no team.
     * **/
    @Nullable Team getTeamFromMember(@NotNull Entity entity);

    /** Tries to return the team where the given {@link UUID} is a member of.
     *
     * @param uuid The {@link UUID} to check
     * @return The {@link Team} the {@link UUID} is a member of, or {@code null} if the {@link UUID} is in no team.
     * **/
    @Nullable Team getTeamFromMember(@NotNull UUID uuid);

    @Contract(pure = true)
    boolean isMember(@NotNull UUID uuid);

    @Contract(pure = true)
    boolean isMember(@NotNull Entity entity);

    /** Returns an immutable collection of teams.
     *
     * @return a {@link ImmutableMap} of teams that might be empty.
     * **/
    @Contract(pure = true)
    @NotNull ImmutableMap<String, Team> getTeams();

    /** Returns a Stream representation of all loaded teams paired with their unique key.
     *
     * @return Stream containing all loaded teams and their keys
     * **/
    @NotNull Stream<Map.Entry<String, Team>> teams();

    boolean joinTeam(@NotNull UUID uuid, @NotNull Team team);

    boolean joinTeam(@NotNull Entity entity, @NotNull Team team);

    boolean leaveTeam(@NotNull UUID uuid, @NotNull Team team);

    boolean leaveTeam(@NotNull Entity entity, @NotNull Team team);

}
