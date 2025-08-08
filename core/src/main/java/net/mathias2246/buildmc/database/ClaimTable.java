package net.mathias2246.buildmc.database;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.mathias2246.buildmc.claims.Claim;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.claims.ClaimType;
import net.mathias2246.buildmc.claims.ProtectionFlag;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ClaimTable implements DatabaseTable {

    private final Cache<Long, Claim> claimCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();

    @Override
    public void createTable(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("""
            CREATE TABLE IF NOT EXISTS claims (
                id IDENTITY PRIMARY KEY,
                type VARCHAR(16) NOT NULL,
                owner_id VARCHAR(64) NOT NULL,
                world_id UUID NOT NULL,
                chunk_x1 INT NOT NULL,
                chunk_z1 INT NOT NULL,
                chunk_x2 INT NOT NULL,
                chunk_z2 INT NOT NULL,
                name VARCHAR(100)
            );
        """);

            stmt.execute("""
            CREATE TABLE IF NOT EXISTS claim_whitelisted_players (
                claim_id BIGINT NOT NULL,
                player_uuid UUID NOT NULL,
                PRIMARY KEY (claim_id, player_uuid),
                FOREIGN KEY (claim_id) REFERENCES claims(id) ON DELETE CASCADE
            );
        """);

            stmt.execute("""
            CREATE TABLE IF NOT EXISTS claim_protection_flags (
                claim_id BIGINT NOT NULL,
                flag VARCHAR(64) NOT NULL,
                PRIMARY KEY (claim_id, flag),
                FOREIGN KEY (claim_id) REFERENCES claims(id) ON DELETE CASCADE
            );
        """);
        }
    }

    public long insertClaim(Connection conn, Claim claim) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("""
        INSERT INTO claims (type, owner_id, world_id, chunk_x1, chunk_z1, chunk_x2, chunk_z2, name)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    """, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, claim.getType().name());
            ps.setString(2, claim.getOwnerId());
            ps.setObject(3, claim.getWorldId());
            ps.setInt(4, claim.getChunkX1());
            ps.setInt(5, claim.getChunkZ1());
            ps.setInt(6, claim.getChunkX2());
            ps.setInt(7, claim.getChunkZ2());
            ps.setString(8, claim.getName());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);

                    // Insert whitelisted players
                    try (PreparedStatement whitelistPS = conn.prepareStatement("""
                    INSERT INTO claim_whitelisted_players (claim_id, player_uuid)
                    VALUES (?, ?)
                """)) {
                        for (UUID uuid : claim.getWhitelistedPlayers()) {
                            whitelistPS.setLong(1, id);
                            whitelistPS.setObject(2, uuid);
                            whitelistPS.addBatch();
                        }
                        whitelistPS.executeBatch();
                    }

                    // Insert protection flags
                    try (PreparedStatement flagPS = conn.prepareStatement("""
                    INSERT INTO claim_protection_flags (claim_id, flag)
                    VALUES (?, ?)
                """)) {
                        for (var flag : claim.getProtectionFlags()) {
                            flagPS.setLong(1, id);
                            flagPS.setString(2, flag.name());
                            flagPS.addBatch();
                        }
                        flagPS.executeBatch();
                    }

                    claimCache.put(id, claim);
                    return id;
                } else {
                    throw new SQLException("Inserting claim failed, no ID obtained.");
                }
            }
        }
    }

    public void deleteClaimById(Connection conn, long claimId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("""
                    DELETE FROM claims WHERE id = ?
                """)) {
            ps.setLong(1, claimId);
            ps.executeUpdate();
        }

        claimCache.invalidate(claimId);
    }

    public Claim getClaimById(Connection conn, long id) throws SQLException {
        Claim cached = claimCache.getIfPresent(id);
        if (cached != null) {
            return cached;
        }

        try (PreparedStatement ps = conn.prepareStatement("""
        SELECT type, owner_id, world_id, chunk_x1, chunk_z1, chunk_x2, chunk_z2, name
        FROM claims WHERE id = ?
    """)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Load related data
                    List<UUID> whitelistedPlayers = new ArrayList<>();
                    EnumSet<ProtectionFlag> protectionFlags = EnumSet.noneOf(ProtectionFlag.class);
                    loadClaimRelations(conn, id, whitelistedPlayers, protectionFlags);

                    Claim claim = new Claim(
                            id,
                            ClaimType.valueOf(rs.getString("type")),
                            rs.getString("owner_id"),
                            (UUID) rs.getObject("world_id"),
                            rs.getInt("chunk_x1"),
                            rs.getInt("chunk_z1"),
                            rs.getInt("chunk_x2"),
                            rs.getInt("chunk_z2"),
                            rs.getString("name"),
                            whitelistedPlayers,
                            protectionFlags
                    );

                    claimCache.put(id, claim);
                    return claim;
                } else {
                    return null;
                }
            }
        }
    }

    private void loadClaimRelations(Connection conn, long claimId,
                                    List<UUID> whitelistedPlayers,
                                    EnumSet<ProtectionFlag> protectionFlags) throws SQLException {
        // Whitelist
        try (PreparedStatement ps = conn.prepareStatement("""
        SELECT player_uuid FROM claim_whitelisted_players WHERE claim_id = ?
    """)) {
            ps.setLong(1, claimId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    whitelistedPlayers.add((UUID) rs.getObject("player_uuid"));
                }
            }
        }

        // Protection Flags
        try (PreparedStatement ps = conn.prepareStatement("""
        SELECT flag FROM claim_protection_flags WHERE claim_id = ?
    """)) {
            ps.setLong(1, claimId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    protectionFlags.add(Enum.valueOf(ProtectionFlag.class, rs.getString("flag")));
                }
            }
        }
    }

    public void invalidateClaim(long id) {
        claimCache.invalidate(id);
    }

    public void updateCache(long id, Claim updatedClaim) {
        claimCache.put(id, updatedClaim);
    }

    public boolean doesClaimExistInArea(Connection conn, UUID worldId, int chunkX1, int chunkZ1, int chunkX2, int chunkZ2) throws SQLException {
        int minX = Math.min(chunkX1, chunkX2);
        int maxX = Math.max(chunkX1, chunkX2);
        int minZ = Math.min(chunkZ1, chunkZ2);
        int maxZ = Math.max(chunkZ1, chunkZ2);

        try (PreparedStatement ps = conn.prepareStatement("""
        SELECT 1 FROM claims
        WHERE world_id = ?
          AND NOT (
            chunk_x2 < ? OR
            chunk_x1 > ? OR
            chunk_z2 < ? OR
            chunk_z1 > ?
          )
        LIMIT 1
    """)) {
            ps.setObject(1, worldId);
            ps.setInt(2, minX);
            ps.setInt(3, maxX);
            ps.setInt(4, minZ);
            ps.setInt(5, maxZ);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<Claim> getOverlappingClaimsInArea(Connection conn, UUID worldId, int x1, int z1, int x2, int z2) throws SQLException {
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minZ = Math.min(z1, z2);
        int maxZ = Math.max(z1, z2);

        List<Claim> overlappingClaims = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement("""
        SELECT id, type, owner_id, world_id, chunk_x1, chunk_z1, chunk_x2, chunk_z2, name
        FROM claims
        WHERE world_id = ?
          AND NOT (
            chunk_x2 < ? OR
            chunk_x1 > ? OR
            chunk_z2 < ? OR
            chunk_z1 > ?
          )
    """)) {
            ps.setObject(1, worldId);
            ps.setInt(2, minX);
            ps.setInt(3, maxX);
            ps.setInt(4, minZ);
            ps.setInt(5, maxZ);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");

                    Claim cached = claimCache.getIfPresent(id);
                    if (cached != null) {
                        overlappingClaims.add(cached);
                        continue;
                    }

                    List<UUID> whitelistedPlayers = new ArrayList<>();
                    EnumSet<ProtectionFlag> protectionFlags = EnumSet.noneOf(ProtectionFlag.class);
                    loadClaimRelations(conn, id, whitelistedPlayers, protectionFlags);

                    Claim claim = new Claim(
                            id,
                            ClaimType.valueOf(rs.getString("type")),
                            rs.getString("owner_id"),
                            (UUID) rs.getObject("world_id"),
                            rs.getInt("chunk_x1"),
                            rs.getInt("chunk_z1"),
                            rs.getInt("chunk_x2"),
                            rs.getInt("chunk_z2"),
                            rs.getString("name"),
                            whitelistedPlayers,
                            protectionFlags
                    );

                    overlappingClaims.add(claim);
                    claimCache.put(id, claim);
                }
            }
        }

        return overlappingClaims;
    }

    public void addWhitelistedPlayer(Connection conn, long claimId, UUID playerUuid) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("""
                MERGE INTO claim_whitelisted_players (claim_id, player_uuid)
                KEY (claim_id, player_uuid)
                VALUES (?, ?)
            """)) {
            ps.setLong(1, claimId);
            ps.setObject(2, playerUuid);
            ps.executeUpdate();
        }

        Claim cached = claimCache.getIfPresent(claimId);
        if (cached != null) {
            cached.getWhitelistedPlayers().add(playerUuid);
            claimCache.put(claimId, cached);
        }
    }

    public void removeWhitelistedPlayer(Connection conn, long claimId, UUID playerUuid) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("""
        DELETE FROM claim_whitelisted_players
        WHERE claim_id = ? AND player_uuid = ?
    """)) {
            ps.setLong(1, claimId);
            ps.setObject(2, playerUuid);
            ps.executeUpdate();
        }

        Claim cached = claimCache.getIfPresent(claimId);
        if (cached != null) {
            cached.getWhitelistedPlayers().remove(playerUuid);
            claimCache.put(claimId, cached);
        }
    }

    public void addProtectionFlag(Connection conn, long claimId, ProtectionFlag flag) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("""
            MERGE INTO claim_protection_flags (claim_id, flag)
            KEY (claim_id, flag)
            VALUES (?, ?)
        """)) {
            ps.setLong(1, claimId);
            ps.setString(2, flag.name());
            ps.executeUpdate();
        }

        Claim cached = claimCache.getIfPresent(claimId);
        if (cached != null) {
            cached.getProtectionFlags().add(flag);
            claimCache.put(claimId, cached);
        }
    }

    public void removeProtectionFlag(Connection conn, long claimId, ProtectionFlag flag) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("""
        DELETE FROM claim_protection_flags
        WHERE claim_id = ? AND flag = ?
    """)) {
            ps.setLong(1, claimId);
            ps.setString(2, flag.name());
            ps.executeUpdate();
        }

        Claim cached = claimCache.getIfPresent(claimId);
        if (cached != null) {
            cached.getProtectionFlags().remove(flag);
            claimCache.put(claimId, cached);
        }
    }

    public void loadClaimOwners(Connection conn) throws SQLException {
        // Temporary maps to populate
        Map<String, List<Long>> teamMap = new HashMap<>();
        Map<UUID, List<Long>> playerMap = new HashMap<>();
        List<Long> serverList = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement("""
        SELECT id, type, owner_id FROM claims
    """)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    ClaimType type = ClaimType.valueOf(rs.getString("type"));
                    String ownerId = rs.getString("owner_id");

                    switch (type) {
                        case TEAM -> teamMap.computeIfAbsent(ownerId, k -> new ArrayList<>()).add(id);
                        case PLAYER -> {
                            UUID playerUuid = UUID.fromString(ownerId);
                            playerMap.computeIfAbsent(playerUuid, k -> new ArrayList<>()).add(id);
                        }
                        case SERVER, PLACEHOLDER -> serverList.add(id);
                    }
                }
            }
        }

        // Set the fields
        ClaimManager.teamOwner = teamMap;
        ClaimManager.playerOwner = playerMap;
        ClaimManager.serverOwner = serverList;
    }

    @Nullable
    public String getClaimNameById(Connection conn, long claimId) throws SQLException {
        // Check the cache first
        Claim cached = claimCache.getIfPresent(claimId);
        if (cached != null) {
            return cached.getName();
        }

        // Query the database if not in cache
        try (PreparedStatement ps = conn.prepareStatement("""
                SELECT name FROM claims WHERE id = ?
            """)) {
            ps.setLong(1, claimId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                } else {
                    return null;
                }
            }
        }
    }
}
