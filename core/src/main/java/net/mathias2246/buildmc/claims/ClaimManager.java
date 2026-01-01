package net.mathias2246.buildmc.claims;

import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.claims.ClaimType;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.api.event.claims.ClaimCreateEvent;
import net.mathias2246.buildmc.api.event.claims.ClaimProtectionChangeEvent;
import net.mathias2246.buildmc.api.event.claims.ClaimRemoveEvent;
import net.mathias2246.buildmc.api.event.claims.ClaimWhitelistChangeEvent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

import static net.mathias2246.buildmc.CoreMain.plugin;

@SuppressWarnings({"unused", "BooleanMethodIsAlwaysInverted"})
@ApiStatus.Internal
public class ClaimManager {

    /** The namespaced key used to store the claim ID inside the chunks PersistentDataContainer */
    public static final @NotNull NamespacedKey CLAIM_PCD_KEY = Objects.requireNonNull(NamespacedKey.fromString("buildmc:claim_id"));

    /** Map of team names and the claim IDs they own */
    public static ConcurrentMap<String, List<Long>> teamOwner;

    /** Map of player UUIDs and the claim IDs they own */
    public static ConcurrentMap<UUID, List<Long>> playerOwner;

    /** Map of team names and the remaining claims */
    public static Map<String, Integer> teamRemainingClaims;

    /** Map of player UUIDs and the remaining claims */
    public static Map<String, Integer> playerRemainingClaims;

    /** List of claim IDs of server claims */
    public static List<Long> serverClaims;

    /** List of claim IDs of placeholder claims */
    public static List<Long> placeholderClaims;

    public static boolean isDimensionBlacklist = true;

    public final static @NotNull List<World> dimensionList = new ArrayList<>();

    public static boolean isWorldAllowed(@NotNull World world) {
        if (isDimensionBlacklist) {
            return !dimensionList.contains(world);
        }
        return dimensionList.contains(world);
    }

    /** Gets the player's team
     * @return the team the player is currently on, or null if they have no team */
    public static @Nullable Team getPlayerTeam(@NotNull Player player) {
        return player.getScoreboard().getEntryTeam(player.getName());
    }

    /**Checks if the given player is allowed on this claim.
     * <p>This means that the player is allowed to do anything on the claim at that location.</p>
     * @return True if, the player is the owner or whitelisted and the ProtectionFlags are set at the given location.*/
    public static boolean isPlayerAllowed(@NotNull Player player, @NotNull Collection<NamespacedKey> protections, Location location) {
        if (player.hasPermission("buildmc.bypass-claims")) return true;

        Claim claim;
        try {
            claim = ClaimManager.getClaim(location);
        } catch (SQLException e) {
            plugin.getLogger().severe("SQL Error while getting claim: " + e);
            return true; // Allow by default on error. Not sure what to do here.
        }

        // Allow if no claim found
        if (claim == null) return true;

        // Allow if claim is a placeholder
        if (claim.getType() == ClaimType.PLACEHOLDER) return true;

        // Allow if player is explicitly whitelisted
        if (claim.getWhitelistedPlayers().contains(player.getUniqueId())) return true;

        String playerId = player.getUniqueId().toString();

        switch (claim.getType()) {
            case SERVER:
                return !hasAllProtectionKeys(claim, protections);

            case PLAYER:
                if (Objects.equals(claim.getOwnerId(), playerId)) return true;
                return !hasAllProtectionKeys(claim, protections);

            case TEAM:
                Team playerTeam = getPlayerTeam(player);
                if (playerTeam != null && Objects.equals(playerTeam.getName(), claim.getOwnerId())) return true;
                return !hasAllProtectionKeys(claim, protections);

            default:
                return true;
        }
    }

    /**Checks if the given player is allowed on this claim.
     * <p>This means that the player is allowed to do anything on the given claim.</p>
     * @return True if, the player is the owner or whitelisted and the ProtectionFlags are set on the given claim.*/
    public static boolean isPlayerAllowed(@NotNull Player player, @NotNull Collection<NamespacedKey> protections, @Nullable Claim claim) {
        if (player.hasPermission("buildmc.bypass-claims")) return true;

        // Allow if no claim found
        if (claim == null) return true;

        // Allow if claim is a placeholder
        if (claim.getType() == ClaimType.PLACEHOLDER) return true;

        // Allow if player is explicitly whitelisted
        if (claim.getWhitelistedPlayers().contains(player.getUniqueId())) return true;

        String playerId = player.getUniqueId().toString();

        switch (claim.getType()) {
            case SERVER:
                return !hasAnyProtection(claim, protections);

            case PLAYER:
                if (Objects.equals(claim.getOwnerId(), playerId)) return true;
                return !hasAnyProtection(claim, protections);

            case TEAM:
                Team playerTeam = getPlayerTeam(player);
                if (playerTeam != null && Objects.equals(playerTeam.getName(), claim.getOwnerId())) return true;
                return !hasAnyProtection(claim, protections);

            default:
                return true;
        }
    }

    /**Checks if the given player is allowed on this claim.
     * <p>This means that the player is allowed to do anything on the given claim.</p>
     * @return True if, the player is the owner or whitelisted and the ProtectionFlags are set on the given claim.*/
    public static boolean isPlayerAllowed(@NotNull Player player, @NotNull NamespacedKey protection, @Nullable Claim claim) {
        // Allow if no claim found
        if (claim == null) return true;

        // Bypass all Protections
        if (player.hasPermission("buildmc.bypass-claims")) return true;

        // Allow if claim is a placeholder
        if (claim.getType() == ClaimType.PLACEHOLDER) return true;

        // Allow if player is explicitly whitelisted
        if (claim.getWhitelistedPlayers().contains(player.getUniqueId())) return true;

        String playerId = player.getUniqueId().toString();

        switch (claim.getType()) {
            case SERVER:
                return !hasProtection(claim, protection);

            case PLAYER:
                if (Objects.equals(claim.getOwnerId(), playerId)) return true;
                return !hasProtection(claim, protection);

            case TEAM:
                Team playerTeam = getPlayerTeam(player);
                if (playerTeam != null && Objects.equals(playerTeam.getName(), claim.getOwnerId())) return true;
                return !hasProtection(claim, protection);

            default:
                return true;
        }
    }

    /**Checks if the given player is allowed on this claim.
     * <p>This means that the player is allowed to do anything on the claim at that location.</p>
     * @return True if, the player is the owner or whitelisted and the ProtectionFlags are set at the given location.*/
    public static boolean isPlayerAllowed(@NotNull Player player, @NotNull NamespacedKey protection, Location location) {
        if (player.hasPermission("buildmc.bypass-claims")) return true;

        Claim claim;
        try {
            claim = ClaimManager.getClaim(location);
        } catch (SQLException e) {
            plugin.getLogger().severe("SQL Error while getting claim: " + e);
            return true; // Allow by default on error. Not sure what to do here.
        }

        // Allow if no claim found
        if (claim == null) return true;

        // Allow if claim is a placeholder
        if (claim.getType() == ClaimType.PLACEHOLDER) return true;

        // Allow if player is explicitly whitelisted
        if (claim.getWhitelistedPlayers().contains(player.getUniqueId())) return true;


        String playerId = player.getUniqueId().toString();

        switch (claim.getType()) {
            case SERVER:
                return !hasProtection(claim, protection);

            case PLAYER:
                if (Objects.equals(claim.getOwnerId(), playerId)) return true;
                return !hasProtection(claim, protection);

            case TEAM:
                Team playerTeam = getPlayerTeam(player);
                if (playerTeam != null && Objects.equals(playerTeam.getName(), claim.getOwnerId())) return true;
                return !hasProtection(claim, protection);

            default:
                return true;
        }
    }

    public static boolean isPlayerAllowedInClaim(@Nullable Claim claim, @NotNull Player player) {
        if (player.hasPermission("buildmc.bypass-claims")) return true;

        // Allow if no claim found
        if (claim == null) return true;

        ClaimType type = claim.getType();

        // Allow if claim is a placeholder
        if (type == ClaimType.PLACEHOLDER || type == ClaimType.SERVER) return true;

        // Allow if player is explicitly whitelisted
        if (claim.getWhitelistedPlayers().contains(player.getUniqueId())) return true;


        String playerId = player.getUniqueId().toString();

        return switch (type) {
            case PLAYER -> Objects.equals(claim.getOwnerId(), playerId);
            case TEAM -> {
                Team playerTeam = getPlayerTeam(player);
                yield (playerTeam != null && Objects.equals(playerTeam.getName(), claim.getOwnerId()));
            }
            default -> true;
        };
    }

    public static boolean hasAnyProtection(Claim claim, Collection<NamespacedKey> protections) {
        for (NamespacedKey key : protections) {
            if (claim.hasProtection(key)) return true;
        }
        return false;
    }

    public static boolean hasProtection(Claim claim, NamespacedKey protection) {
        return claim.hasProtection(protection);
    }

    public static boolean hasAllProtections(Claim claim, Collection<String> flags) {
        return new HashSet<>(claim.getProtections()).containsAll(flags);
    }

    public static boolean hasAllProtectionKeys(Claim claim, Collection<NamespacedKey> keys) {
        for (var f : keys) {
            if (!claim.hasProtection(f)) return false;
        }
        return true;
    }

    /** @return True if a claim is in the given area.*/
    public static boolean isClaimInArea(UUID worldID, int chunkX1, int chunkZ1, int chunkX2, int chunkZ2) throws SQLException {
        return CoreMain.claimTable.doesClaimExistInArea(CoreMain.databaseManager.getConnection(), worldID, chunkX1, chunkZ1, chunkX2, chunkZ2);
    }

    /**@return A list of claims in the given area. Is empty if not found.
     * @throws SQLException If an internal database error occurred.
     * @throws IllegalArgumentException If any of the locations are null, or they're not in the same world.*/
    public static List<Claim> getClaimsInArea(Location pos1, Location pos2) throws SQLException, IllegalArgumentException {
        if (pos1 == null || pos2 == null) {
            throw new IllegalArgumentException("Positions cannot be null.");
        }

        if (pos1.getWorld() == null || pos2.getWorld() == null) {
            throw new IllegalArgumentException("Both locations must have a world.");
        }

        UUID worldId1 = pos1.getWorld().getUID();
        UUID worldId2 = pos2.getWorld().getUID();

        if (!worldId1.equals(worldId2)) {
            throw new IllegalArgumentException("Locations must be in the same world.");
        }

        int chunkX1 = pos1.getBlockX() >> 4;
        int chunkZ1 = pos1.getBlockZ() >> 4;
        int chunkX2 = pos2.getBlockX() >> 4;
        int chunkZ2 = pos2.getBlockZ() >> 4;

        return CoreMain.claimTable.getOverlappingClaimsInArea(
                CoreMain.databaseManager.getConnection(),
                worldId1,
                chunkX1, chunkZ1,
                chunkX2, chunkZ2
        );
    }

    /**@return True if, the given chunk is claimed*/
    public static boolean isClaimed(Chunk chunk) {
        return chunk.getPersistentDataContainer().has(CLAIM_PCD_KEY);
    }

    /**@return The id of the claim, or null if not claimed.*/
    @Nullable public static Long getClaimId(Chunk chunk) {
        return chunk.getPersistentDataContainer().get(CLAIM_PCD_KEY, PersistentDataType.LONG);
    }

    /**@return The claim on this chunk, or null if not claimed.
     * @throws SQLException If an internal database error occurred.*/
    @Nullable public static Claim getClaim(Chunk chunk) throws SQLException {
        var claimId = getClaimId(chunk);
        if (claimId == null) return null;

        return CoreMain.claimTable.getClaimById(CoreMain.databaseManager.getConnection(), claimId);
    }

    @Nullable public static Claim getClaimByID(long claimID) {
        try {
            return CoreMain.claimTable.getClaimById(CoreMain.databaseManager.getConnection(), claimID);
        } catch (SQLException e) {
            plugin.getLogger().severe("SQL Error while trying to get a claim by ID: " + e);
        }
        return null;
    }

    /**
     * @throws SQLException If an internal database error occurred.
     */
    @Nullable
    public static Claim getClaim(@NotNull Location location) throws SQLException {
        World world = location.getWorld();

        if (world == null) {
            throw new IllegalArgumentException("Location does not belong to a world.");
        }

        if (!world.isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
            return null;
        }

        Chunk chunk = location.getChunk(); // safe now
        return getClaim(chunk);
    }


    public static List<Claim> getAllClaims() throws SQLException {
        return CoreMain.claimTable.getAllClaims(CoreMain.databaseManager.getConnection());
    }

    /**Tries to claim the given area for the given player.
     * @return True, if successfully claimed the area.*/
    public static boolean tryClaimPlayerArea(@NotNull Player player, String claimName, Location pos1, Location pos2) {
        return tryClaimArea(ClaimType.PLAYER, player.getUniqueId().toString(), claimName, pos1, pos2);
    }

    /**Tries to claim the given area for the given team.
     * @return True, if successfully claimed the area.*/
    public static boolean tryClaimTeamArea(@NotNull Team team, String claimName, Location pos1, Location pos2) {
        return tryClaimArea(ClaimType.TEAM, team.getName(), claimName, pos1, pos2);
    }

    public static boolean tryClaimServerArea(String claimName, Location pos1, Location pos2) {
        return tryClaimArea(ClaimType.SERVER, "server", claimName, pos1, pos2);
    }

    public static boolean tryClaimPlaceholderArea(String claimName, Location pos1, Location pos2) {
        return tryClaimArea(ClaimType.PLACEHOLDER, "server", claimName, pos1, pos2);
    }

    public static boolean tryClaimArea(@NotNull ClaimType type, @NotNull String ownerId, @NotNull String claimName, @NotNull Location pos1, @NotNull Location pos2) {
        if (pos1.getWorld() == null || pos2.getWorld() == null) return false;

        if (pos1.getWorld() != pos2.getWorld()) return false;

        UUID worldId = pos1.getWorld().getUID();

        int chunkX1 = pos1.getBlockX() >> 4;
        int chunkZ1 = pos1.getBlockZ() >> 4;
        int chunkX2 = pos2.getBlockX() >> 4;
        int chunkZ2 = pos2.getBlockZ() >> 4;

        Claim claim = new Claim(
                null,
                type,
                ownerId,
                worldId,
                chunkX1,
                chunkZ1,
                chunkX2,
                chunkZ2,
                claimName,
                List.of(),
                new ArrayList<>()
        );

        long claimId;
        try {
            claimId = CoreMain.claimTable.insertClaim(CoreMain.databaseManager.getConnection(), claim);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to insert claim into database: " + e);
            return false;
        }

        if (claimId == -1) return false;

        claim.setID(claimId);

        // Set persistent chunk data
        var startX = Math.min(chunkX1, chunkX2);
        var endX = Math.max(chunkX1, chunkX2);
        var startZ = Math.min(chunkZ1, chunkZ2);
        var endZ = Math.max(chunkZ1, chunkZ2);

        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                var chunk = pos1.getWorld().getChunkAt(x, z);
                var pdc = chunk.getPersistentDataContainer();
                pdc.set(CLAIM_PCD_KEY, PersistentDataType.LONG, claimId);
            }
        }

        ClaimCreateEvent event = new ClaimCreateEvent(
                claim
        );
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        // Update ownership mapping
        switch (type) {
            case PLAYER -> {
                UUID uuid = UUID.fromString(ownerId);
                playerOwner.computeIfAbsent(uuid, k -> new ArrayList<>()).add(claimId);
            }
            case TEAM -> teamOwner.computeIfAbsent(ownerId, k -> new ArrayList<>()).add(claimId);

            case SERVER -> serverClaims.add(claimId);
            case PLACEHOLDER -> placeholderClaims.add(claimId);
        }

        return true;
    }

    /**Adds a player to a Claim whitelist by claimID.*/
    public static void addPlayerToWhitelist(long claimID, UUID playerID) {
        Claim claim = null;

        try {
            claim = CoreMain.claimTable.getClaimById(CoreMain.databaseManager.getConnection(), claimID);
        } catch (SQLException e) {
            plugin.getLogger().severe("SQL error while getting claim: " + e);
        }

        if (claim == null) return;

        ClaimWhitelistChangeEvent event = new ClaimWhitelistChangeEvent(claim, Bukkit.getOfflinePlayer(playerID), ClaimWhitelistChangeEvent.ChangeAction.ADDED);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        claim.addWhitelistedPlayer(playerID);

        try {
            CoreMain.claimTable.addWhitelistedPlayer(CoreMain.databaseManager.getConnection(), claimID, playerID);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**Removes a player from a claims whitelist by claimID.*/
    public static void removePlayerFromWhitelist(long claimID, UUID playerID) {
        Claim claim = null;

        try {
            claim = CoreMain.claimTable.getClaimById(CoreMain.databaseManager.getConnection(), claimID);
        } catch (SQLException e) {
            plugin.getLogger().severe("SQL error while getting claim: " + e);
        }

        if (claim == null || claim.getId() == null) return;

        ClaimWhitelistChangeEvent event = new ClaimWhitelistChangeEvent(claim, Bukkit.getOfflinePlayer(playerID), ClaimWhitelistChangeEvent.ChangeAction.REMOVED);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        claim.removeWhitelistedPlayer(playerID);

        try {
            CoreMain.claimTable.removeWhitelistedPlayer(CoreMain.databaseManager.getConnection(), claimID, playerID);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addProtection(@NotNull Claim claim, @NotNull NamespacedKey protectionKey) {
        Protection protection = CoreMain.protectionsRegistry.get(protectionKey);
        if (protection == null) {
            throw new IllegalArgumentException("No protection exists with key " + protectionKey);
        }

        addProtection(claim, protection);
    }

    public static void addProtection(@NotNull Claim claim, @NotNull Protection protection) {
        Long claimId = claim.getId();
        if (claimId == null) {
            throw new IllegalArgumentException("Claim has no ID: " + claim.getName());
        }

        ClaimProtectionChangeEvent event =
                new ClaimProtectionChangeEvent(
                        claim,
                        protection,
                        ClaimProtectionChangeEvent.ActiveState.ENABLED
                );

        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        try {
            CoreMain.claimTable.addProtectionFlag(
                    CoreMain.databaseManager.getConnection(),
                    claimId,
                    protection.getKey()
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addProtection(long claimId, @NotNull Protection protection) {
        Claim claim = getClaimByID(claimId);
        if (claim == null) {
            throw new IllegalArgumentException("No claim exists with id " + claimId);
        }
        ClaimProtectionChangeEvent event = new ClaimProtectionChangeEvent(claim, protection, ClaimProtectionChangeEvent.ActiveState.ENABLED);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        try {
            CoreMain.claimTable.addProtectionFlag(CoreMain.databaseManager.getConnection(), claimId, protection.getKey());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addProtection(long claimId, @NotNull NamespacedKey protection) {
        Claim claim = getClaimByID(claimId);
        if (claim == null) {
            throw new IllegalArgumentException("No claim exists with id " + claimId);
        }

        Protection protectionObject = CoreMain.protectionsRegistry.get(protection);
        if (protectionObject == null) {
            throw new IllegalArgumentException("No protection exists with key " + protection);
        }

        ClaimProtectionChangeEvent event = new ClaimProtectionChangeEvent(claim, protectionObject, ClaimProtectionChangeEvent.ActiveState.ENABLED);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        try {
            CoreMain.claimTable.addProtectionFlag(CoreMain.databaseManager.getConnection(), claimId, protection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeProtection(@NotNull Claim claim, @NotNull NamespacedKey protectionKey) {
        Protection protection = CoreMain.protectionsRegistry.get(protectionKey);
        if (protection == null) {
            throw new IllegalArgumentException("No protection exists with key " + protectionKey);
        }

        removeProtection(claim, protection);
    }

    public static void removeProtection(@NotNull Claim claim, @NotNull Protection protection) {
        Long claimId = claim.getId();
        if (claimId == null) {
            throw new IllegalArgumentException("Claim has no ID: " + claim.getName());
        }

        ClaimProtectionChangeEvent event =
                new ClaimProtectionChangeEvent(
                        claim,
                        protection,
                        ClaimProtectionChangeEvent.ActiveState.DISABLED
                );

        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        try {
            CoreMain.claimTable.removeProtectionFlag(
                    CoreMain.databaseManager.getConnection(),
                    claimId,
                    protection.getKey()
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeProtection(long claimId, @NotNull Protection protection) {
        Claim claim = getClaimByID(claimId);
        if (claim == null) {
            throw new IllegalArgumentException("No claim exists with id " + claimId);
        }

        ClaimProtectionChangeEvent event = new ClaimProtectionChangeEvent(claim, protection, ClaimProtectionChangeEvent.ActiveState.DISABLED);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        try {
            CoreMain.claimTable.removeProtectionFlag(CoreMain.databaseManager.getConnection(), claimId, protection.getKey());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeProtection(long claimId, @NotNull NamespacedKey protection) {
        Claim claim = getClaimByID(claimId);
        if (claim == null) {
            throw new IllegalArgumentException("No claim exists with id " + claimId);
        }

        Protection protectionObject = CoreMain.protectionsRegistry.get(protection);
        if (protectionObject == null) {
            throw new IllegalArgumentException("No protection exists with key " + protection);
        }

        ClaimProtectionChangeEvent event = new ClaimProtectionChangeEvent(claim, protectionObject, ClaimProtectionChangeEvent.ActiveState.ENABLED);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        try {
            CoreMain.claimTable.removeProtectionFlag(CoreMain.databaseManager.getConnection(), claimId, protection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**Tries to get the name of the claim by claimID.
     * @return The name of the claim, or null if not found.*/
    @Nullable public static String getClaimNameById(long claimId) {
        String name = null;

        try {
            name = CoreMain.claimTable.getClaimNameById(CoreMain.databaseManager.getConnection(), claimId);
        } catch (SQLException e) {
            plugin.getLogger().severe("SQL Error while trying to get name by ID " + e);
        }
        return name;
    }

    /**Removes a claim by its claimID.
     * @return True, if successfully removed claim.*/
    public static boolean removeClaimById(long claimId) {
        ClaimRemoveEvent event = new ClaimRemoveEvent(Objects.requireNonNull(getClaimByID(claimId)));
        Bukkit.getPluginManager().callEvent(event);
        try {
            CoreMain.claimTable.deleteClaimById(CoreMain.databaseManager.getConnection(), claimId);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to remove claim from database: " + e);
            return false;
        }
        return true;
    }

    public static boolean doesOwnerHaveClaimWithName(String ownerId, String claimName) throws SQLException {
        return CoreMain.claimTable.doesOwnerHaveClaimWithName(CoreMain.databaseManager.getConnection(), ownerId, claimName);
    }

    public static @Nullable Integer getRemainingTeamClaims(String teamName) {
        return teamRemainingClaims.get(teamName);
    }

    public static @Nullable Integer getRemainingPlayerClaims(String playerUUID) {
        return playerRemainingClaims.get(playerUUID);
    }

    public static @Nullable Integer getRemainingPlayerClaims(UUID playerUUID) {
        return playerRemainingClaims.get(playerUUID.toString());
    }

    public static void setRemainingTeamClaims(String teamName, @Nullable Integer remainingClaims) {
        if (remainingClaims == null) {
            teamRemainingClaims.remove(teamName);
        } else {
            teamRemainingClaims.put(teamName, remainingClaims);
        }
    }

    public static void setRemainingPlayerClaims(String playerUUID, @Nullable Integer remainingClaims) {
        if (remainingClaims == null) {
            playerRemainingClaims.remove(playerUUID);
        } else {
            playerRemainingClaims.put(playerUUID, remainingClaims);
        }
    }

    public static void setRemainingPlayerClaims(UUID playerUUID, @Nullable Integer remainingClaims) {
        setRemainingPlayerClaims(playerUUID.toString(), remainingClaims);
    }

}
