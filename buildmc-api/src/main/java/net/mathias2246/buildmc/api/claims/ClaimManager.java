package net.mathias2246.buildmc.api.claims;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * The {@code ClaimAPI} provides methods to interact with land claims,
 * protections, and whitelists within the BuildMC plugin.
 * <p>
 * It allows checking claim ownership, player permissions, protections, and
 * managing claims through players or teams.
 */
public interface ClaimManager {

    /**
     * Gets the current scoreboard team of a player.
     *
     * @param player the player
     * @return the team the player is in, or {@code null} if none
     */
    @Nullable Team getPlayerTeam(@NotNull Player player);

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
     * @param protection the protection (protection) to check
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
     * @throws IllegalArgumentException  if any of the locations are null, or they're not in the same world.
     */
    List<Claim> getClaimsInArea(Location pos1, Location pos2) throws SQLException, IllegalArgumentException;

    /**
     * Checks if a chunk is claimed.
     *
     * @param chunk the chunk
     * @return {@code true} if the chunk is claimed
     */
    boolean isClaimed(Chunk chunk);

    /**
     * Gets the claim ID of a claimed chunk.
     *
     * @param chunk the chunk
     * @return the claim ID, or {@code null} if unclaimed
     */
    @Nullable Long getClaimId(Chunk chunk);

    /**
     * Gets the claim associated with a chunk.
     *
     * @param chunk the chunk
     * @return the claim
     * @throws SQLException if a database error occurs
     */
    @Nullable Claim getClaim(Chunk chunk) throws SQLException;

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
     * @param location the location
     * @return the claim, or {@code null} if unclaimed
     */
    @Nullable Claim getClaim(Location location) throws SQLException;

    /**
     * Attempts to create a claim for a player between two positions.
     *
     * @param player    the player
     * @param claimName the claim name
     * @param pos1      first corner location
     * @param pos2      opposite corner location
     * @return {@code true} if the claim was created successfully
     * @throws IllegalArgumentException  if any of the locations are null, or they're not in the same world.
     */
    boolean tryClaimPlayerArea(@NotNull Player player, String claimName, Location pos1, Location pos2) throws IllegalArgumentException;

    /**
     * Attempts to create a claim for a team between two positions.
     *
     * @param team      the team
     * @param claimName the claim name
     * @param pos1      first corner location
     * @param pos2      opposite corner location
     * @return {@code true} if the claim was created successfully
     * @throws IllegalArgumentException  if any of the locations are null, or they're not in the same world.
     */
    boolean tryClaimTeamArea(@NotNull Team team, String claimName, Location pos1, Location pos2) throws IllegalArgumentException;

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
     * Adds a protection to a claim.
     *
     * @param claim      the claim
     * @param protection the protection
     */
    void addProtection(@NotNull Claim claim, @NotNull Protection protection);

    /**
     * Adds a protection to a claim.
     *
     * @param claim      the claim
     * @param protection the protection key
     */
    void addProtection(@NotNull Claim claim, @NotNull NamespacedKey protection);

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
     * Removes a protection from a claim.
     *
     * @param claim      the claim
     * @param protection the protection
     */
    void removeProtection(@NotNull Claim claim, @NotNull Protection protection);

    /**
     * Removes a protection from a claim.
     *
     * @param claim      the claim
     * @param protection the protection key
     */
    void removeProtection(@NotNull Claim claim, @NotNull NamespacedKey protection);

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
}
