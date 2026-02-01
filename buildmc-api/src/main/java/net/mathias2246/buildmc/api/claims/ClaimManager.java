package net.mathias2246.buildmc.api.claims;

import net.mathias2246.buildmc.api.event.claims.ClaimCreateEvent;
import net.mathias2246.buildmc.api.event.claims.ClaimRemoveEvent;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The {@link ClaimManager} provides methods to interact with land claims,
 * protections, and whitelists within the BuildMC plugin.
 * <p>
 * It allows checking claim ownership, player permissions, protections, and
 * managing claims through players or teams.
 */
public interface ClaimManager {

    /**
     * Gets the current scoreboard {@link Team} of a player.
     *
     * @param player the player
     * @return the {@link Team} the player is in, or {@code null} if none
     */
    @Nullable Team getPlayerTeam(@NotNull Player player);

    /**
     * Gets the name of the owner of the given claim.
     *
     * @param claim the claim to get the owners name from.
     * @return the name of the claim owner
     */
    @NotNull String getOwnerName(@NotNull Claim claim);

    /**
     * Checks whether a player is allowed to create a claim in a specific world.
     * <p>
     *     Players can bypass this by using the <i>'buildmc.bypass-claim-dimension-list'</i> permission.
     * </p>
     *
     * @param world The {@link World} to check
     * @return {@code true} if, claims can be created by anyone in the given world.
     * */
    boolean isWorldAllowed(@NotNull World world);

    /**
     * Checks whether a player is allowed to be inside the given claim.
     * If the player is not the owner, not whitelisted or doesn't bypass protections, he won't be allowed.
     * <p>
     *     This will only check on {@link ClaimType#PLAYER} or {@link ClaimType#SERVER}.
     * </p>
     *
     * @param claim       the {@link Claim} to check
     * @param player      the player
     * @return {@code true} if the player is allowed, otherwise {@code false}
     */
    boolean isPlayerAllowedInClaim(@Nullable Claim claim, @NotNull Player player);

    /**
     * Checks whether a player is allowed to perform an action at a specific location
     * given multiple protection keys.
     * If all provided protections are false, the player is allowed.
     *
     * @param player      the player
     * @param protections the protections to check
     * @param location    the location
     * @return {@code true} if the player is allowed, otherwise {@code false}
     */
    boolean isPlayerAllowed(@NotNull Player player, @NotNull Collection<NamespacedKey> protections, Location location);

    /**
     * Checks whether a player is allowed to perform an action in a claim
     * given multiple protection keys.
     * If all provided protections are false, the player is allowed.
     *
     * @param player      the player
     * @param protections the protections to check
     * @param claim       the claim, or {@code null} if outside a claim
     * @return {@code true} if the player is allowed, otherwise {@code false}
     */
    boolean isPlayerAllowed(@NotNull Player player, @NotNull Collection<NamespacedKey> protections, @Nullable Claim claim);

    /**
     * Checks whether a player is allowed to perform an action in a given claim
     * for a single protection key.
     *
     * @param player     the player
     * @param protection the protection to check
     * @param claim      the claim, or {@code null} if outside a claim
     * @return {@code true} if the player is allowed, otherwise {@code false}
     */
    boolean isPlayerAllowed(@NotNull Player player, @NotNull NamespacedKey protection, @Nullable Claim claim);

    /**
     * Checks whether a player is allowed to perform an action at a location
     * for a single protection key.
     *
     * @param player     the player
     * @param protection the protection to check
     * @param location   the location
     * @return {@code true} if the player is allowed, otherwise {@code false}
     */
    boolean isPlayerAllowed(@NotNull Player player, @NotNull NamespacedKey protection, Location location);

    /**
     * Checks if a claim has any of the provided protections.
     *
     * @param claim       the claim
     * @param protections the protections (protections) to check
     * @return {@code true} if the claim has any of the protections
     */
    boolean hasAnyProtection(Claim claim, Collection<NamespacedKey> protections);

    /**
     * Checks if a claim has a specific protection.
     *
     * @param claim      the claim
     * @param protection the protection to check
     * @return {@code true} if the claim has the protection
     */
    boolean hasProtection(Claim claim, NamespacedKey protection);

    /**
     * Checks if a claim has all the specified protections by string identifiers.
     *
     * @param claim the claim
     * @param protections the protections to check
     * @return {@code true} if the claim has all the protections
     */
    boolean hasAllProtections(Claim claim, Collection<String> protections);

    /**
     * Checks if a claim has all the specified protections by key.
     *
     * @param claim the claim
     * @param keys  the protection keys to check
     * @return {@code true} if the claim has all the protections
     */
    boolean hasAllProtectionKeys(Claim claim, Collection<NamespacedKey> keys);

    /**
     * Checks if there is any claim within the given chunk area.
     *
     * @param worldID the world UUID
     * @param chunkX1 first corner chunk X
     * @param chunkZ1 first corner chunk Z
     * @param chunkX2 opposite corner chunk X
     * @param chunkZ2 opposite corner chunk Z
     * @return {@code true} if a claim exists in the area
     * @throws SQLException if a database error occurs
     */
    boolean isClaimInArea(UUID worldID, int chunkX1, int chunkZ1, int chunkX2, int chunkZ2) throws SQLException;

    /**
     * Gets all claims within the given area.
     *
     * @param pos1 first corner location
     * @param pos2 opposite corner location
     * @return list of claims in the area
     * @throws SQLException              if a database error occurs
     * @throws IllegalArgumentException  if any of the {@link Location}s are null, or they're not in the same world.
     */
    List<Claim> getClaimsInArea(Location pos1, Location pos2) throws SQLException, IllegalArgumentException;

    /**
     * Checks if a {@link Location} is claimed.
     *
     * @param loc a location
     * @return {@code true} if the chunk is claimed
     */
    boolean isClaimed(@NotNull Location loc);

    /**
     * Gets the claim ID of a claimed {@link Location}.
     *
     * @param loc a location
     * @return the claim ID, or {@code null} if unclaimed
     */
    @Nullable Long getClaimId(@NotNull Location loc);


    /**
     * Gets a claim by its ID.
     *
     * @param claimID the claim ID
     * @return the claim, or {@code null} if none exists
     */
    @Nullable Claim getClaimByID(long claimID);

    /**
     * Gets the claim at a given location.
     *
     * @param location a location
     * @return the claim, or {@code null} if unclaimed
     */
    @Nullable Claim getClaim(@NotNull Location location);

    /**
     * Retrieves all {@link Claim} entries stored in the database.
     *
     * <p><b>Note:</b> This method may be resource-intensive if a large number of claims exist.
     * Consider using it primarily during startup, data synchronization, or administrative tasks.
     * </p>
     *
     * @return a list containing all {@link Claim} objects found in the database.
     * @throws SQLException if a database access error occurs while retrieving claims.
     */
    List<Claim> getAllClaims() throws SQLException;

    /**
     * Attempts to create a claim of some {@link ClaimType} between two positions.
     *
     * @param type    the {@link ClaimType} of the claim
     * @param claimOwner the id of the claim owner.
     *                   <p>
     *                   "Server" for {@link ClaimType#SERVER} or {@link ClaimType#PLACEHOLDER}.
     *                   </p>
     *                   <p>
     *                   The team id for {@link ClaimType#TEAM}, or the player {@link UUID} for {@link ClaimType#PLAYER}.
     *                   </p>
     * @param claimName the claim name
     * @param pos1      first corner location
     * @param pos2      opposite corner location
     * @return The ID of the claim. Or {@code null} if the claim was not created successfully
     * @throws IllegalArgumentException  if any of the {@link Location}s are null, or they're not in the same world.
     */
    @Nullable Long tryClaimArea(@NotNull ClaimType type, @NotNull String claimOwner, @NotNull String claimName, Location pos1, Location pos2) throws IllegalArgumentException;

    /**
     * Attempts to register a {@link Claim} instance.
     *
     * @param claim The {@link Claim} to register. All values of the claim are validated.
     * @return The ID of the claim. Or {@code null} if the claim was not registered successfully
     */
    @Nullable Long tryClaimArea(@NotNull Claim claim);

    /**
     * Attempts to create a claim for a player between two positions.
     *
     * @param player    the player
     * @param claimName the claim name
     * @param pos1      first corner location
     * @param pos2      opposite corner location
     * @return The ID of the claim. Or {@code null} if the claim was not created successfully
     * @throws IllegalArgumentException  if any of the {@link Location}s are null, or they're not in the same world.
     */
    @Nullable Long tryClaimPlayerArea(@NotNull Player player, @NotNull String claimName, Location pos1, Location pos2) throws IllegalArgumentException;

    /**
     * Attempts to create a claim for a {@link Team} between two positions.
     *
     * @param team      the team
     * @param claimName the claim name
     * @param pos1      first corner location
     * @param pos2      opposite corner location
     * @return The ID of the claim. Or {@code null} if the claim was not created successfully
     * @throws IllegalArgumentException  if any of the {@link Location}s are null, or they're not in the same world.
     */
    @Nullable Long tryClaimTeamArea(@NotNull Team team, @NotNull String claimName, Location pos1, Location pos2) throws IllegalArgumentException;

    /**
     * Attempts to create a claim owned by the server between two positions.
     *
     * @param claimName the claim name
     * @param pos1      first corner location
     * @param pos2      opposite corner location
     * @return The ID of the claim. Or {@code null} if the claim was not created successfully
     * @throws IllegalArgumentException if any of the {@link Location}s are null, or they're not in the same world
     */
    @Nullable Long tryClaimServerArea(@NotNull String claimName, Location pos1, Location pos2) throws IllegalArgumentException;

    /**
     * Attempts to create a placeholder claim between two positions.
     * <p>
     * A {@link ClaimType#PLACEHOLDER} claim may be used to reserve an area temporarily
     * without assigning it to a player, team, or the server.
     * </p>
     *
     * @param claimName the claim name
     * @param pos1      first corner location
     * @param pos2      opposite corner location
     * @return The ID of the claim. Or {@code null} if the claim was not created successfully
     * @throws IllegalArgumentException if any of the {@link Location}s are null, or they're not in the same world
     */
    @Nullable Long tryClaimPlaceholderArea(@NotNull String claimName, @NotNull Location pos1, @NotNull Location pos2) throws IllegalArgumentException;

    /**
     * Registers multiple {@link Claim} objects in the system and persists them to the database.
     * <p>
     * This method will first fire a {@link ClaimCreateEvent} containing all provided claims.
     * If the event is cancelled by any listener, the registration process is aborted
     * and {@code null} is returned.
     * </p>
     * <p>
     * If not cancelled, all claims are inserted into the database in a batch operation.
     * The returned map links each {@link Claim} to its newly assigned database ID.
     * </p>
     * <p><b>Important:</b> You are responsible for supplying fully initialized {@link Claim}
     * instances. This method only handles persistence and cache registration; it does not
     * validate claim boundaries, ownership rules, or overlap constraints.</p>
     *
     * @param claims the list of claims to register
     * @return a map of each claim to its generated database ID, or {@code null} if the creation event was cancelled
     * @throws SQLException if a database access error occurs during insertion
     */
    Map<Claim, Long> registerClaims(List<Claim> claims) throws SQLException;

    /**
     * Deletes multiple {@link Claim} objects from the system and removes them from the database.
     * <p>
     * If the provided collection is {@code null} or empty, the method returns immediately
     * without performing any action.
     * </p>
     * <p>
     * A {@link ClaimRemoveEvent} is fired before deletion, allowing listeners to react
     * to or track the removal. This event is informational and does not prevent deletion.
     * </p>
     * <p>
     * Each claim must already have a valid database ID. If any claim has a {@code null}
     * ID, an {@link IllegalArgumentException} is thrown and no database operation is performed.
     * </p>
     * <p><b>Important:</b> You are responsible for ensuring the claims are valid and should
     * be removed. This method only handles persistence and cache removal.</p>
     *
     * @param claims the collection of claims to delete
     * @throws SQLException if a database access error occurs during deletion
     * @throws IllegalArgumentException if any claim does not have a valid ID
     */
    void deleteClaims(Collection<Claim> claims) throws SQLException, IllegalArgumentException;

    /**
     * Adds a player to a claim's whitelist.
     *
     * @param claimID  the claim ID
     * @param playerID the player UUID
     */
    void addPlayerToWhitelist(long claimID, UUID playerID);

    /**
     * Removes a player from a claim's whitelist.
     *
     * @param claimID  the claim ID
     * @param playerID the player UUID
     */
    void removePlayerFromWhitelist(long claimID, UUID playerID);

    /**
     * Adds a protection to a claim by ID.
     *
     * @param claim    the claim
     * @param protection the protection key
     */
    void addProtection(@NotNull Claim claim, @NotNull NamespacedKey protection);

    /**
     * Adds a protection to a claim by ID.
     *
     * @param claim    the claim
     * @param protection the protection
     */
    void addProtection(@NotNull Claim claim, @NotNull Protection protection);

    /**
     * Adds a protection to a claim by ID.
     *
     * @param claimId    the claim ID
     * @param protection the protection
     */
    void addProtection(long claimId, @NotNull Protection protection);

    /**
     * Adds a protection to a claim by ID.
     *
     * @param claimId    the claim ID
     * @param protection the protection key
     */
    void addProtection(long claimId, @NotNull NamespacedKey protection);

    /**
     * Removes a protection from a claim by ID.
     *
     * @param claim    the claim
     * @param protection the protection key
     */
    void removeProtection(@NotNull Claim claim, @NotNull NamespacedKey protection);

    /**
     * Removes a protection from a claim by ID.
     *
     * @param claim    the claim
     * @param protection the protection
     */
    void removeProtection(@NotNull Claim claim, @NotNull Protection protection);

    /**
     * Removes a protection from a claim by ID.
     *
     * @param claimId    the claim ID
     * @param protection the protection
     */
    void removeProtection(long claimId, @NotNull Protection protection);

    /**
     * Removes a protection from a claim by ID.
     *
     * @param claimId    the claim ID
     * @param protection the protection key
     */
    void removeProtection(long claimId, @NotNull NamespacedKey protection);

    /**
     * Gets the name of a claim by its ID.
     *
     * @param claimId the claim ID
     * @return the claim name
     */
    @Nullable String getClaimNameById(long claimId);

    /**
     * Removes a claim by its ID.
     *
     * @param claimId the claim ID
     * @return {@code true} if the claim was removed successfully
     */
    boolean removeClaimById(long claimId);

    /**
     * Checks whether the owner already has a claim with the given name.
     *
     * @param ownerId   the owner identifier
     * @param claimName the claim name
     * @return {@code true} if the owner already has a claim with that name
     * @throws SQLException if a database error occurs
     */
    boolean doesOwnerHaveClaimWithName(String ownerId, String claimName) throws SQLException;

    /**
     * Gets how many claimable chunks a team has left.
     *
     * @param teamName the team name
     * @return the number of remaining claims available to the team,
     *         or {@code null} if no data is available
     */
    @Nullable Integer getRemainingTeamClaims(String teamName);

    /**
     * Gets how many claimable chunks a player has left.
     *
     * @param playerUUID the player's UUID as a string
     * @return the number of remaining claims available to the player,
     *         or {@code null} if no data is available
     */
    @Nullable Integer getRemainingPlayerClaims(String playerUUID);

    /**
     * Gets how many claimable chunks a player has left.
     *
     * @param playerUUID the player's UUID
     * @return the number of remaining claims available to the player,
     *         or {@code null} if no data is available
     */
    @Nullable Integer getRemainingPlayerClaims(UUID playerUUID);

    /**
     * Sets the number of claimable chunks a team has left.
     * <p>
     * If {@code remainingClaims} is {@code null}, the team's remaining claim
     * record is removed.
     * </p>
     *
     * @param teamName        the team name
     * @param remainingClaims the new number of remaining claims,
     *                        or {@code null} to remove the record
     */
    void setRemainingTeamClaims(String teamName, @Nullable Integer remainingClaims);

    /**
     * Sets the number of claimable chunks a player has left.
     * <p>
     * If {@code remainingClaims} is {@code null}, the player's remaining claim
     * record is removed.
     * </p>
     *
     * @param playerUUID      the player's UUID as a string
     * @param remainingClaims the new number of remaining claims,
     *                        or {@code null} to remove the record
     */
    void setRemainingPlayerClaims(String playerUUID, @Nullable Integer remainingClaims);

    /**
     * Sets the number of claimable chunks a player has left.
     * <p>
     * If {@code remainingClaims} is {@code null}, the player's remaining claim
     * record is removed.
     * </p>
     *
     * @param playerUUID      the player's UUID
     * @param remainingClaims the new number of remaining claims,
     *                        or {@code null} to remove the record
     */
    void setRemainingPlayerClaims(UUID playerUUID, @Nullable Integer remainingClaims);

    /**
     * Update the name of a Claim
     *
     * @param claim   the claim
     * @param newName the new claim name
     * @throws SQLException if a database error occurs
     * @throws IllegalArgumentException if the claim has no ID
     */
    void updateClaimName(@NotNull Claim claim,  @NotNull String newName) throws SQLException, IllegalArgumentException;

    /**
     * Update the name of a Claim
     *
     * @param claimId the claim ID
     * @param newName the new claim name
     * @throws SQLException if a database error occurs
     */
    void updateClaimName(long claimId,  @NotNull String newName) throws SQLException;

    /**
     * Update the owner of a Claim
     *
     * @param claim   the claim
     * @param newOwnerId the new owner ID (usually a player UUID as string, a team name or "server" depending on the {@link ClaimType})
     * @throws SQLException if a database error occurs
     * @throws IllegalArgumentException if the claim has no ID
     */
    void updateClaimOwner(@NotNull Claim claim, @NotNull String newOwnerId) throws SQLException, IllegalArgumentException;

    /**
     * Update the owner of a Claim
     *
     * @param claimId   the claim ID
     * @param newOwnerId the new owner ID (usually a player UUID as string, a team name or "server" depending on the {@link ClaimType})
     * @throws SQLException if a database error occurs
     */
    void updateClaimOwner(long claimId, @NotNull String newOwnerId) throws SQLException;
}
