package net.mathias2246.buildmc.database;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.claims.ClaimType;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.util.LocationUtil;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static net.mathias2246.buildmc.claims.ClaimManager.*;

@SuppressWarnings({"unused"})
public class ClaimTable implements DatabaseTable {

    private final Cache<@NotNull Long, @NotNull Claim> claimCache = CacheBuilder.newBuilder()
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

            stmt.execute("""
            CREATE INDEX IF NOT EXISTS idx_claims_owner_id\s
            ON claims(owner_id);
       \s""");

            stmt.execute("""
            CREATE INDEX IF NOT EXISTS idx_claims_world_id\s
            ON claims(world_id);
       \s""");

            stmt.execute("""
            CREATE INDEX IF NOT EXISTS idx_claims_chunks\s
            ON claims(world_id, chunk_x1, chunk_z1, chunk_x2, chunk_z2);
       \s""");
            stmt.execute("""
            CREATE INDEX IF NOT EXISTS idx_whitelisted_claim_id\s
            ON claim_whitelisted_players(claim_id);
       \s""");
            // Index for reverse lookups by player (optional, but useful if you search claims by player)
            stmt.execute("""
            CREATE INDEX IF NOT EXISTS idx_whitelisted_player_uuid\s
            ON claim_whitelisted_players(player_uuid);
       \s""");
            stmt.execute("""
            CREATE INDEX IF NOT EXISTS idx_flags_claim_id\s
            ON claim_protection_flags(claim_id);
       \s""");
        }
    }

    @Override
     public void prepareStatements(Connection connection) throws SQLException {
        insertClaimPs = connection.prepareStatement("""
            INSERT INTO claims (type, owner_id, world_id, chunk_x1, chunk_z1, chunk_x2, chunk_z2, name)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """,
        Statement.RETURN_GENERATED_KEYS
        );
        insertWhitelistPs = connection.prepareStatement("""
            INSERT INTO claim_whitelisted_players (claim_id, player_uuid)
            VALUES (?, ?)
        """);
        insertProtectionsPs = connection.prepareStatement("""
            INSERT INTO claim_protection_flags (claim_id, flag)
            VALUES (?, ?)
        """);

        getClaimByIdPs = connection.prepareStatement("""
            SELECT * FROM claims WHERE id = ?
        """);
        getClaimNameByIdPs = connection.prepareStatement("""
            SELECT name FROM claims WHERE id = ?
        """);

        loadClaimRelationsWhitelistPs = connection.prepareStatement("""
            SELECT player_uuid FROM claim_whitelisted_players WHERE claim_id = ?
        """);
        loadClaimRelationsProtectionsPs = connection.prepareStatement("""
            SELECT flag FROM claim_protection_flags WHERE claim_id = ?
        """);

        getClaimOwnerPs = connection.prepareStatement("""
            SELECT id, type, owner_id FROM claims
        """);
        doesOwnerHaveClaimWithNamePs = connection.prepareStatement("""
            SELECT 1 FROM claims
            WHERE owner_id = ? AND name = ?
            LIMIT 1
        """);

        doesClaimExistInAreaPs = connection.prepareStatement("""
            SELECT 1 FROM claims
            WHERE world_id = ?
              AND NOT (
                chunk_x2 <= ? OR
                chunk_x1 >= ? OR
                chunk_z2 <= ? OR
                chunk_z1 >= ?
              )
            LIMIT 1
        """);

        getOverlappingClaimsPs = connection.prepareStatement("""
            SELECT id, type, owner_id, world_id,
                   chunk_x1, chunk_z1, chunk_x2, chunk_z2, name
            FROM claims
            WHERE world_id = ?
              AND LEAST(chunk_x1, chunk_x2) <= ?
              AND GREATEST(chunk_x1, chunk_x2) >= ?
              AND LEAST(chunk_z1, chunk_z2) <= ?
              AND GREATEST(chunk_z1, chunk_z2) >= ?
        """);

        addProtectionPs = connection.prepareStatement("""
            MERGE INTO claim_protection_flags (claim_id, flag)
            KEY (claim_id, flag)
            VALUES (?, ?)
        """);

        removeProtectionPs = connection.prepareStatement("""
            DELETE FROM claim_protection_flags
            WHERE claim_id = ? AND flag = ?
        """);
    }

    @Override
    public void closeStatements(Connection connection) throws SQLException {
        getOverlappingClaimsPs.close();
        getClaimOwnerPs.close();
        getClaimNameByIdPs.close();
        doesOwnerHaveClaimWithNamePs.close();
        doesClaimExistInAreaPs.close();
        getClaimByIdPs.close();
        loadClaimRelationsProtectionsPs.close();
        loadClaimRelationsWhitelistPs.close();
        insertClaimPs.close();
        insertProtectionsPs.close();
        insertWhitelistPs.close();
        addProtectionPs.close();
        removeProtectionPs.close();
    }

    // Inserts entries into tables
    private PreparedStatement insertClaimPs;
    private PreparedStatement insertWhitelistPs;
    private PreparedStatement insertProtectionsPs;

    public long insertClaim(Connection conn, Claim claim) throws SQLException {
        PreparedStatement ps = insertClaimPs;

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
                PreparedStatement whitelistPS = insertWhitelistPs;
                for (UUID uuid : claim.getWhitelistedPlayers()) {
                    whitelistPS.setLong(1, id);
                    whitelistPS.setObject(2, uuid);
                    whitelistPS.addBatch();
                }
                whitelistPS.executeBatch();

                // Insert protection flags
                PreparedStatement flagPS = insertProtectionsPs;
                for (var flag : claim.getProtections()) {
                    flagPS.setLong(1, id);
                    flagPS.setString(2, flag);
                    flagPS.addBatch();
                }
                flagPS.executeBatch();

                claimCache.put(id, claim);

                updateRemainingClaims(claim);

                return id;
            } else {
                throw new SQLException("Inserting claim failed, no ID obtained.");
            }
        }
    }

    private void updateRemainingClaims(Claim claim) {
        // Calculate claimed area in chunks - pls work
        int width = Math.abs(claim.getChunkX2() - claim.getChunkX1()) + 1;
        int height = Math.abs(claim.getChunkZ2() - claim.getChunkZ1()) + 1;
        int claimedArea = width * height;

        String ownerId = claim.getOwnerId();
        if (ownerId == null || ownerId.isEmpty()) {
            CoreMain.plugin.getLogger().warning("Claim inserted without ownerId. Skipping remaining claims update.");
            return;
        }

        switch (claim.getType()) {
            case TEAM -> {
                int maxChunks = CoreMain.plugin.getConfig().getInt("claims.team-max-chunk-claim-amount", 0);
                teamRemainingClaims.compute(ownerId, (team, remaining) -> {
                    if (remaining == null) {
                        remaining = maxChunks; // Initialize
                    }
                    int newValue = remaining - claimedArea;
                    return Math.max(newValue, 0);
                });
            }
            case PLAYER -> {
                int maxChunks = CoreMain.plugin.getConfig().getInt("claims.player-max-chunk-claim-amount", 0);
                playerRemainingClaims.compute(ownerId, (player, remaining) -> {
                    if (remaining == null) {
                        remaining = maxChunks; // Initialize
                    }
                    int newValue = remaining - claimedArea;
                    return Math.max(newValue, 0);
                });
            }
        }
    }

    public void deleteClaimById(Connection conn, long claimId) throws SQLException {
        // Fetch claim
        Claim claim = getClaimById(conn, claimId);
        if (claim == null) {
            CoreMain.plugin.getLogger().warning("Attempted to delete claim ID " + claimId + " but it was not found.");
            return;
        }

        try (PreparedStatement ps = conn.prepareStatement("""
            DELETE FROM claims WHERE id = ?
        """)) {
            ps.setLong(1, claimId);
            ps.executeUpdate();
        }

        // Invalidate cache
        claimCache.invalidate(claimId);

        // Remove from ownership mappings
        switch (claim.getType()) {
            case PLAYER -> {
                UUID uuid = UUID.fromString(claim.getOwnerId());
                var claims = playerOwner.get(uuid);
                if (claims != null) {
                    claims.remove(claimId);
                    if (claims.isEmpty()) {
                        playerOwner.remove(uuid);
                    }
                }
            }
            case TEAM -> {
                var claims = teamOwner.get(claim.getOwnerId());
                if (claims != null) {
                    claims.remove(claimId);
                    if (claims.isEmpty()) {
                        teamOwner.remove(claim.getOwnerId());
                    }
                }
            }
            case SERVER -> serverClaims.remove(claimId);
            case PLACEHOLDER -> placeholderClaims.remove(claimId);
        }

        // Update remaining claims (cleanup chunks etc.)
        restoreRemainingClaims(claim);
    }



    private void restoreRemainingClaims(Claim claim) {
        // Calculate claimed area in chunks
        int width = Math.abs(claim.getChunkX2() - claim.getChunkX1()) + 1;
        int height = Math.abs(claim.getChunkZ2() - claim.getChunkZ1()) + 1;
        int claimedArea = width * height;

        String ownerId = claim.getOwnerId();
        if (ownerId == null || ownerId.isEmpty()) {
            CoreMain.plugin.getLogger().warning("Claim deletion without ownerId. Skipping remaining claims restore.");
            return;
        }

        switch (claim.getType()) {
            case TEAM -> {
                int maxChunks = CoreMain.plugin.getConfig().getInt("claims.team-max-chunk-claim-amount", 0);
                teamRemainingClaims.compute(ownerId, (team, remaining) -> {
                    if (remaining == null) {
                        remaining = maxChunks; // Initialize
                    }
                    int newValue = remaining + claimedArea;
                    return Math.min(newValue, maxChunks); // Do not exceed max
                });
            }
            case PLAYER -> {
                int maxChunks = CoreMain.plugin.getConfig().getInt("claims.player-max-chunk-claim-amount", 0);
                playerRemainingClaims.compute(ownerId, (player, remaining) -> {
                    if (remaining == null) {
                        remaining = maxChunks; // Initialize
                    }
                    int newValue = remaining + claimedArea;
                    return Math.min(newValue, maxChunks); // Do not exceed max
                });
            }
        }
    }

    private PreparedStatement getClaimByIdPs; // Statement for getting a claim instance using its id

    public Claim getClaimById(Connection conn, long id) throws SQLException {
        Claim cached = claimCache.getIfPresent(id);
        if (cached != null) {
            return cached;
        }

        PreparedStatement ps = getClaimByIdPs;
        ps.setLong(1, id);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                // Load related data
                List<UUID> whitelistedPlayers = new ArrayList<>();
                List<String> protections = new ArrayList<>();
                loadClaimRelations(conn, id, whitelistedPlayers, protections);

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
                        protections
                );

                claimCache.put(id, claim);
                return claim;
            } else {
                return null;
            }
        }
    }

    private PreparedStatement loadClaimRelationsWhitelistPs;
    private PreparedStatement loadClaimRelationsProtectionsPs;

    private void loadClaimRelations(Connection conn, long claimId,
                                    Collection<UUID> whitelistedPlayers,
                                    Collection<String> protections) throws SQLException {
        // Whitelist
        PreparedStatement ps = loadClaimRelationsWhitelistPs;
        ps.setLong(1, claimId);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                whitelistedPlayers.add((UUID) rs.getObject("player_uuid"));
            }
        }


        // Protection Flags
        PreparedStatement ps2 = loadClaimRelationsProtectionsPs;
        ps2.setLong(1, claimId);
        try (ResultSet rs = ps2.executeQuery()) {
            while (rs.next()) {
                protections.add(rs.getString("flag"));
            }
        }
    }

    public List<Claim> getAllClaims(Connection conn) throws SQLException {
        List<Claim> allClaims = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement("""
        SELECT id, type, owner_id, world_id, chunk_x1, chunk_z1, chunk_x2, chunk_z2, name
        FROM claims
    """)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");

                    // Use cache if present
                    Claim cached = claimCache.getIfPresent(id);
                    if (cached != null) {
                        allClaims.add(cached);
                        continue;
                    }

                    // Otherwise, load relations and build the claim
                    List<UUID> whitelistedPlayers = new ArrayList<>();
                    List<String> protections = new ArrayList<>();
                    loadClaimRelations(conn, id, whitelistedPlayers, protections);

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
                            protections
                    );

                    claimCache.put(id, claim);
                    allClaims.add(claim);
                }
            }
        }

        return allClaims;
    }


    public void invalidateClaim(long id) {
        claimCache.invalidate(id);
    }

    public void updateCache(long id, Claim updatedClaim) {
        claimCache.put(id, updatedClaim);
    }

    private PreparedStatement doesClaimExistInAreaPs;

    public boolean doesClaimExistInArea(Connection conn, UUID worldId, int chunkX1, int chunkZ1, int chunkX2, int chunkZ2) throws SQLException {
        int minX = Math.min(chunkX1, chunkX2);
        int maxX = Math.max(chunkX1, chunkX2);
        int minZ = Math.min(chunkZ1, chunkZ2);
        int maxZ = Math.max(chunkZ1, chunkZ2);

        PreparedStatement ps = doesClaimExistInAreaPs;
        ps.setObject(1, worldId);
        ps.setInt(2, maxX);
        ps.setInt(3, minX);
        ps.setInt(4, maxZ);
        ps.setInt(5, minZ);

        try (ResultSet rs = ps.executeQuery()) {
            return rs.next();
        }
    }

    private PreparedStatement getOverlappingClaimsPs;

    public List<Claim> getOverlappingClaimsInArea(
            Connection conn,
            UUID worldId,
            int x1, int z1,
            int x2, int z2
    ) throws SQLException {

        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minZ = Math.min(z1, z2);
        int maxZ = Math.max(z1, z2);

        List<Claim> overlappingClaims = new ArrayList<>();

        PreparedStatement ps = getOverlappingClaimsPs;
        ps.setObject(1, worldId);

        ps.setInt(2, maxX); // existing.minX <= new.maxX
        ps.setInt(3, minX); // existing.maxX >= new.minX
        ps.setInt(4, maxZ); // existing.minZ <= new.maxZ
        ps.setInt(5, minZ); // existing.maxZ >= new.minZ

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                long id = rs.getLong("id");

                Claim cached = claimCache.getIfPresent(id);
                if (cached != null) {
                    overlappingClaims.add(cached);
                    continue;
                }

                List<UUID> whitelistedPlayers = new ArrayList<>();
                List<String> protections = new ArrayList<>();
                loadClaimRelations(conn, id, whitelistedPlayers, protections);

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
                        protections
                );

                overlappingClaims.add(claim);
                claimCache.put(id, claim);
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
    }

    private PreparedStatement addProtectionPs;

    public void addProtectionFlag(Connection conn, long claimId, @NotNull NamespacedKey protectionKey) throws SQLException {
        PreparedStatement ps = addProtectionPs;
        ps.setLong(1, claimId);
        ps.setString(2, protectionKey.toString());
        ps.executeUpdate();


        Claim cached = claimCache.getIfPresent(claimId);
        if (cached != null) {
            cached.addProtection(protectionKey);
            claimCache.put(claimId, cached);
        }
    }

    private PreparedStatement removeProtectionPs;
    public void removeProtectionFlag(Connection conn, long claimId, @NotNull NamespacedKey protectionKey) throws SQLException {
        PreparedStatement ps = removeProtectionPs;
        ps.setLong(1, claimId);
        ps.setString(2, protectionKey.toString());
        ps.executeUpdate();


        Claim cached = claimCache.getIfPresent(claimId);
        if (cached != null) {
            cached.removeProtection(protectionKey);
            claimCache.put(claimId, cached);
        }
    }

    private PreparedStatement getClaimOwnerPs;

    public void loadClaimOwners(Connection conn) throws SQLException {
        // Temporary maps to populate
        Map<String, List<Long>> teamMap = new HashMap<>();
        Map<UUID, List<Long>> playerMap = new HashMap<>();
        List<Long> serverList = new ArrayList<>();
        List<Long> placeholderList = new ArrayList<>();

        PreparedStatement ps = getClaimOwnerPs;
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
                    case SERVER -> serverList.add(id);
                    case PLACEHOLDER -> placeholderList.add(id);
                }
            }
        }

        // Set the fields
        teamOwner = teamMap;
        ClaimManager.playerOwner = playerMap;
        ClaimManager.serverClaims = serverList;
        ClaimManager.placeholderClaims = placeholderList;
    }

    private PreparedStatement getClaimNameByIdPs; // Statement for getting a claim name using its id

    @Nullable
    public String getClaimNameById(Connection conn, long claimId) throws SQLException {
        // Check the cache first
        Claim cached = claimCache.getIfPresent(claimId);
        if (cached != null) {
            return cached.getName();
        }

        // Query the database if not in cache
        PreparedStatement ps = getClaimNameByIdPs;
        ps.setLong(1, claimId);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString("name");
            } else {
                return null;
            }
        }
    }


    private PreparedStatement doesOwnerHaveClaimWithNamePs;

    public boolean doesOwnerHaveClaimWithName(Connection conn, String ownerId, String claimName) throws SQLException {
        PreparedStatement ps = doesOwnerHaveClaimWithNamePs;
        ps.setString(1, ownerId);
        ps.setString(2, claimName);

        try (ResultSet rs = ps.executeQuery()) {
            return rs.next();
        }
    }

    public static void calculateRemainingClaims(Connection conn) throws SQLException {
        teamRemainingClaims = new HashMap<>();
        playerRemainingClaims = new HashMap<>();

        // Initialize with the max
        for (String team : teamOwner.keySet()) {
            int maxChunks = CoreMain.plugin.getConfig().getInt("claims.team-max-chunk-claim-amount", 0);
            teamRemainingClaims.put(team, maxChunks);
        }
        for (UUID player : playerOwner.keySet()) {
            int maxChunks = CoreMain.plugin.getConfig().getInt("claims.player-max-chunk-claim-amount", 0);
            playerRemainingClaims.put(player.toString(), maxChunks);
        }

        String sql = "SELECT type, owner_id, chunk_x1, chunk_z1, chunk_x2, chunk_z2 FROM claims";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ClaimType type = ClaimType.valueOf(rs.getString("type"));
                String ownerId = rs.getString("owner_id");
                int x1 = rs.getInt("chunk_x1");
                int z1 = rs.getInt("chunk_z1");
                int x2 = rs.getInt("chunk_x2");
                int z2 = rs.getInt("chunk_z2");

                int chunkCount = LocationUtil.calculateChunkArea(x1, z1, x2, z2);

                switch (type) {
                    case TEAM -> {
                        int maxChunks = CoreMain.plugin.getConfig().getInt("claims.team-max-chunk-claim-amount");
                        int remaining = teamRemainingClaims.getOrDefault(ownerId, maxChunks);
                        teamRemainingClaims.put(ownerId, Math.max(remaining - chunkCount, 0));
                    }
                    case PLAYER -> {
                        UUID playerUuid = UUID.fromString(ownerId);
                        int maxChunks = CoreMain.plugin.getConfig().getInt("claims.player-max-chunk-claim-amount");
                        int remaining = playerRemainingClaims.getOrDefault(playerUuid.toString(), maxChunks);
                        playerRemainingClaims.put(playerUuid.toString(), Math.max(remaining - chunkCount, 0));
                    }
                    default -> {
                        // Ignore others
                    }
                }
            }
        }
    }
}
