package net.mathias2246.buildmc.claims;

import net.mathias2246.buildmc.CoreMain;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.*;

@SuppressWarnings({"unused", "BooleanMethodIsAlwaysInverted"})
public class ClaimManager {

    /** The namespaced key used to store the claim ID inside the chunks PersistentDataContainer */
    public static final @NotNull NamespacedKey CLAIM_PCD_KEY = Objects.requireNonNull(NamespacedKey.fromString("buildmc:claim_id"));

    /** Map of team names and the claim IDs they own */
    public static Map<String, List<Long>> teamOwner;

    /** Map of player UUIDs and the claim IDs they own */
    public static Map<UUID, List<Long>> playerOwner;

    /** List of claim IDs the server owns */
    public static List<Long> serverOwner;

    /** Gets the player's team
     * @return the team the player is currently on, or null if they have no team */
    public static @Nullable Team getPlayerTeam(@NotNull Player player) {
        return player.getScoreboard().getEntryTeam(player.getName());
    }

    /**Checks if the given player is allowed on this claim.
     * <p>This means that the player is allowed to do anything on the claim at that location.</p>
     * @return True if, the player is the owner or whitelisted and the ProtectionFlags are set at the given location.*/
    public static boolean isPlayerAllowed(@NotNull Player player, @NotNull EnumSet<ProtectionFlag> protectionFlags, Location location) {
        if (player.hasPermission("buildmc.bypass-claims")) return true;

        Claim claim;
        try {
            claim = ClaimManager.getClaim(location);
        } catch (SQLException e) {
            CoreMain.plugin.getLogger().severe("SQL Error while getting claim: " + e.getMessage());
            return true; // Allow by default on error. Not sure what to do here.
        }

        // Allow if no claim found
        if (claim == null) return true;

        // Allow if player is explicitly whitelisted
        if (claim.getWhitelistedPlayers().contains(player.getUniqueId())) return true;

        // Allow if claim is a placeholder
        if (claim.getType() == ClaimType.PLACEHOLDER) return true;

        String playerId = player.getUniqueId().toString();

        switch (claim.getType()) {
            case SERVER:
                return !hasAllFlags(claim, protectionFlags);

            case PLAYER:
                if (Objects.equals(claim.getOwnerId(), playerId)) return true;
                return !hasAllFlags(claim, protectionFlags);

            case TEAM:
                Team playerTeam = getPlayerTeam(player);
                if (playerTeam != null && Objects.equals(playerTeam.getName(), claim.getOwnerId())) return true;
                return !hasAllFlags(claim, protectionFlags);

            default:
                return true;
        }
    }

    /**Checks if the given player is allowed on this claim.
     * <p>This means that the player is allowed to do anything on the given claim.</p>
     * @return True if, the player is the owner or whitelisted and the ProtectionFlags are set on the given claim.*/
    public static boolean isPlayerAllowed(@NotNull Player player, @NotNull EnumSet<ProtectionFlag> protectionFlags, @Nullable Claim claim) {
        if (player.hasPermission("buildmc.bypass-claims")) return true;

        // Allow if no claim found
        if (claim == null) return true;

        // Allow if player is explicitly whitelisted
        if (claim.getWhitelistedPlayers().contains(player.getUniqueId())) return true;

        // Allow if claim is a placeholder
        if (claim.getType() == ClaimType.PLACEHOLDER) return true;

        String playerId = player.getUniqueId().toString();

        switch (claim.getType()) {
            case SERVER:
                return !hasAnyFlag(claim, protectionFlags);

            case PLAYER:
                if (Objects.equals(claim.getOwnerId(), playerId)) return true;
                return !hasAnyFlag(claim, protectionFlags);

            case TEAM:
                Team playerTeam = getPlayerTeam(player);
                if (playerTeam != null && Objects.equals(playerTeam.getName(), claim.getOwnerId())) return true;
                return !hasAnyFlag(claim, protectionFlags);

            default:
                return true;
        }
    }

    /**Checks if the given player is allowed on this claim.
     * <p>This means that the player is allowed to do anything on the given claim.</p>
     * @return True if, the player is the owner or whitelisted and the ProtectionFlags are set on the given claim.*/
    public static boolean isPlayerAllowed(@NotNull Player player, @NotNull ProtectionFlag protectionFlag, @Nullable Claim claim) {
        if (player.hasPermission("buildmc.bypass-claims")) return true;

        // Allow if no claim found
        if (claim == null) return true;

        // Allow if player is explicitly whitelisted
        if (claim.getWhitelistedPlayers().contains(player.getUniqueId())) return true;

        // Allow if claim is a placeholder
        if (claim.getType() == ClaimType.PLACEHOLDER) return true;

        String playerId = player.getUniqueId().toString();

        switch (claim.getType()) {
            case SERVER:
                return !hasFlag(claim, protectionFlag);

            case PLAYER:
                if (Objects.equals(claim.getOwnerId(), playerId)) return true;
                return !hasFlag(claim, protectionFlag);

            case TEAM:
                Team playerTeam = getPlayerTeam(player);
                if (playerTeam != null && Objects.equals(playerTeam.getName(), claim.getOwnerId())) return true;
                return !hasFlag(claim, protectionFlag);

            default:
                return true;
        }
    }

    /**Checks if the given player is allowed on this claim.
     * <p>This means that the player is allowed to do anything on the claim at that location.</p>
     * @return True if, the player is the owner or whitelisted and the ProtectionFlags are set at the given location.*/
    public static boolean isPlayerAllowed(@NotNull Player player, @NotNull ProtectionFlag protectionFlag, Location location) {
        if (player.hasPermission("buildmc.bypass-claims")) return true;

        Claim claim;
        try {
            claim = ClaimManager.getClaim(location);
        } catch (SQLException e) {
            CoreMain.plugin.getLogger().severe("SQL Error while getting claim: " + e.getMessage());
            return true; // Allow by default on error. Not sure what to do here.
        }

        // Allow if no claim found
        if (claim == null) return true;

        // Allow if player is explicitly whitelisted
        if (claim.getWhitelistedPlayers().contains(player.getUniqueId())) return true;

        // Allow if claim is a placeholder
        if (claim.getType() == ClaimType.PLACEHOLDER) return true;

        String playerId = player.getUniqueId().toString();

        switch (claim.getType()) {
            case SERVER:
                return !hasFlag(claim, protectionFlag);

            case PLAYER:
                if (Objects.equals(claim.getOwnerId(), playerId)) return true;
                return !hasFlag(claim, protectionFlag);

            case TEAM:
                Team playerTeam = getPlayerTeam(player);
                if (playerTeam != null && Objects.equals(playerTeam.getName(), claim.getOwnerId())) return true;
                return !hasFlag(claim, protectionFlag);

            default:
                return true;
        }
    }

    private static boolean hasAnyFlag(Claim claim, EnumSet<ProtectionFlag> flags) {
        for (ProtectionFlag flag : flags) {
            if (claim.hasFlag(flag)) return true;
        }
        return false;
    }

    private static boolean hasFlag(Claim claim, ProtectionFlag flag) {
        return claim.hasFlag(flag);
    }

    private static boolean hasAllFlags(Claim claim, EnumSet<ProtectionFlag> flags) {
        return claim.getProtectionFlags().containsAll(flags);
    }

    /** @return True if a claim is in the given area.*/
    public static boolean isClaimInArea(UUID worldID, int chunkX1, int chunkZ1, int chunkX2, int chunkZ2) throws SQLException {
        return CoreMain.claimTable.doesClaimExistInArea(CoreMain.databaseManager.getConnection(), worldID, chunkX1, chunkZ1, chunkX2, chunkZ2);
    }

    /**@return A list of claims in the given area. Is empty if not found.
     * @throws SQLException If an internal database error occurred.
     * @throws IllegalArgumentException If any of the locations is null, or their not in the same world.*/
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
            CoreMain.plugin.getLogger().severe("SQL Error while trying to get a claim by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * @throws SQLException If an internal database error occurred.*/
    @Nullable public static Claim getClaim(Location location) throws SQLException {
        Chunk chunk = location.getChunk();
        return getClaim(chunk);
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

    private static boolean tryClaimArea(@NotNull ClaimType type, @NotNull String ownerId, @NotNull String claimName, @NotNull Location pos1, @NotNull Location pos2) {
        if (pos1.getWorld() == null || pos2.getWorld() == null) return false;

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
                EnumSet.noneOf(ProtectionFlag.class)
        );

        long claimId;
        try {
            claimId = CoreMain.claimTable.insertClaim(CoreMain.databaseManager.getConnection(), claim);
        } catch (SQLException e) {
            CoreMain.plugin.getLogger().severe("Failed to insert claim into database: " + e.getMessage());
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

        // Update ownership mapping
        switch (type) {
            case PLAYER -> {
                UUID uuid = UUID.fromString(ownerId);
                playerOwner.computeIfAbsent(uuid, k -> new ArrayList<>()).add(claimId);
            }
            case TEAM -> teamOwner.computeIfAbsent(ownerId, k -> new ArrayList<>()).add(claimId);
        }

        return true;
    }

    /**Adds a player to a Claim whitelist by claimID.*/
    public static void addPlayerToWhitelist(long claimID, UUID playerID) {
        Claim claim = null;

        try {
            claim = CoreMain.claimTable.getClaimById(CoreMain.databaseManager.getConnection(), claimID);
        } catch (SQLException e) {
            CoreMain.plugin.getLogger().severe("SQL error while getting claim: " + e.getMessage());
        }

        if (claim == null) return;

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
            CoreMain.plugin.getLogger().severe("SQL error while getting claim: " + e.getMessage());
        }

        if (claim == null) return;

        claim.removeWhitelistedPlayer(playerID);

        try {
            CoreMain.claimTable.removeWhitelistedPlayer(CoreMain.databaseManager.getConnection(), claimID, playerID);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addProtectionFlag(long claimId, ProtectionFlag flag) {
        try {
            CoreMain.claimTable.addProtectionFlag(CoreMain.databaseManager.getConnection(), claimId, flag);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addProtectionFlag(Claim claim, ProtectionFlag flag) {
        try {
            CoreMain.claimTable.addProtectionFlag(CoreMain.databaseManager.getConnection(), claim.getId(), flag);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeProtectionFlag(long claimId, ProtectionFlag flag) {
        try {
            CoreMain.claimTable.removeProtectionFlag(CoreMain.databaseManager.getConnection(), claimId, flag);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeProtectionFlag(Claim claim, ProtectionFlag flag) {
        try {
            CoreMain.claimTable.removeProtectionFlag(CoreMain.databaseManager.getConnection(), claim.getId(), flag);
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
            CoreMain.plugin.getLogger().severe("SQL Error while trying to get name by ID " + e.getMessage());
        }
        return name;
    }

    /**Removes a claim by its claimID.
     * @return True, if successfully removed claim.*/
    public static boolean removeClaimById(long claimId) {
        try {
            CoreMain.claimTable.deleteClaimById(CoreMain.databaseManager.getConnection(), claimId);
        } catch (SQLException e) {
            CoreMain.plugin.getLogger().severe("Failed to remove claim from database: " + e.getMessage());
            return false;
        }
        return true;
    }
}
