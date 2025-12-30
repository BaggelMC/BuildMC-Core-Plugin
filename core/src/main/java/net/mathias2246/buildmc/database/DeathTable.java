package net.mathias2246.buildmc.database;

import net.mathias2246.buildmc.deaths.DeathRecord;
import net.mathias2246.buildmc.deaths.DeathSummary;

import java.sql.*;
import java.util.*;

public class DeathTable implements DatabaseTable {

    private PreparedStatement insertDeathPS;
    private PreparedStatement insertDeathItemPS;
    private PreparedStatement getDeathByIdPS;
    private PreparedStatement getDeathItemsPS;
    private PreparedStatement getDeathsByPlayerPS;
    private PreparedStatement getDeathIdsByPlayerPS;
    private PreparedStatement deleteDeathPS;

    @Override
    public void createTable(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS player_deaths (
                    id IDENTITY PRIMARY KEY,
                    player_uuid UUID NOT NULL,
                    timestamp BIGINT NOT NULL,
                    xp INT NOT NULL,
                    cause VARCHAR(64)
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS death_items (
                    death_id BIGINT NOT NULL,
                    slot INT NOT NULL,
                    item BLOB NOT NULL,
                    PRIMARY KEY (death_id, slot),
                    FOREIGN KEY (death_id) REFERENCES player_deaths(id) ON DELETE CASCADE
                );
            """);

            stmt.execute("""
                CREATE INDEX IF NOT EXISTS idx_player_deaths_player
                ON player_deaths(player_uuid);
            """);

            stmt.execute("""
                CREATE INDEX IF NOT EXISTS idx_death_items_death
                ON death_items(death_id);
            """);
        }
    }

    @Override
    public void prepareStatements(Connection conn) throws SQLException {

        insertDeathPS = conn.prepareStatement("""
                INSERT INTO player_deaths (player_uuid, timestamp, xp, cause)
                VALUES (?, ?, ?, ?)
            """, Statement.RETURN_GENERATED_KEYS);

        insertDeathItemPS = conn.prepareStatement("""
                INSERT INTO death_items (death_id, slot, item)
                VALUES (?, ?, ?)
            """);

        getDeathByIdPS = conn.prepareStatement("""
                SELECT * FROM player_deaths WHERE id = ?
            """);
        ;

        getDeathItemsPS = conn.prepareStatement("""
                SELECT slot, item FROM death_items WHERE death_id = ?
            """);

        getDeathsByPlayerPS = conn.prepareStatement("""
                SELECT id, timestamp, xp, cause
                FROM player_deaths
                WHERE player_uuid = ?
                ORDER BY timestamp DESC
            """);

        getDeathIdsByPlayerPS = conn.prepareStatement("""
                SELECT id
                FROM player_deaths
                WHERE player_uuid = ?
                ORDER BY timestamp DESC
            """);

        deleteDeathPS = conn.prepareStatement("""
                DELETE FROM player_deaths WHERE id = ?
            """);
    }

    @Override
    public void closeStatements(Connection conn) throws SQLException {
        getDeathByIdPS.close();
        getDeathItemsPS.close();
        getDeathsByPlayerPS.close();
        getDeathIdsByPlayerPS.close();
        insertDeathPS.close();
        insertDeathItemPS.close();
        deleteDeathPS.close();
    }

    @SuppressWarnings("UnusedReturnValue")
    public long insertDeath(
            Connection conn,
            UUID playerUuid,
            int xp,
            String cause,
            Map<Integer, byte[]> items
    ) throws SQLException {

        conn.setAutoCommit(false);

        try {
            insertDeathPS.setObject(1, playerUuid);
            insertDeathPS.setLong(2, System.currentTimeMillis());
            insertDeathPS.setInt(3, xp);
            insertDeathPS.setString(4, cause);
            insertDeathPS.executeUpdate();

            try (ResultSet rs = insertDeathPS.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("Failed to get death ID");
                }

                long deathId = rs.getLong(1);

                for (var entry : items.entrySet()) {
                    insertDeathItemPS.setLong(1, deathId);
                    insertDeathItemPS.setInt(2, entry.getKey());
                    insertDeathItemPS.setBytes(3, entry.getValue());
                    insertDeathItemPS.addBatch();
                }

                insertDeathItemPS.executeBatch();

                conn.commit();
                return deathId;
            }
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public DeathRecord getDeathById(Connection conn, long id) throws SQLException {

        getDeathByIdPS.setLong(1, id);

        try (ResultSet rs = getDeathByIdPS.executeQuery()) {
            if (!rs.next()) {
                return null;
            }

            Map<Integer, byte[]> items = new HashMap<>();

            getDeathItemsPS.setLong(1, id);
            try (ResultSet irs = getDeathItemsPS.executeQuery()) {
                while (irs.next()) {
                    items.put(
                            irs.getInt("slot"),
                            irs.getBytes("item")
                    );
                }
            }

            return new DeathRecord(
                    id,
                    (UUID) rs.getObject("player_uuid"),
                    rs.getLong("timestamp"),
                    rs.getInt("xp"),
                    rs.getString("cause"),
                    items
            );
        }
    }

    public List<DeathSummary> getDeathsByPlayer(Connection conn, UUID playerUuid) throws SQLException {

        List<DeathSummary> deaths = new ArrayList<>();

        getDeathsByPlayerPS.setObject(1, playerUuid);

        try (ResultSet rs = getDeathsByPlayerPS.executeQuery()) {
            while (rs.next()) {
                deaths.add(new DeathSummary(
                        rs.getLong("id"),
                        rs.getLong("timestamp"),
                        rs.getInt("xp"),
                        rs.getString("cause")
                ));
            }
        }

        return deaths;
    }

    public List<Long> getDeathIdsByPlayer(Connection conn, UUID playerUuid) throws SQLException {

        List<Long> ids = new ArrayList<>();

        getDeathIdsByPlayerPS.setObject(1, playerUuid);

        try (ResultSet rs = getDeathIdsByPlayerPS.executeQuery()) {
            while (rs.next()) {
                ids.add(rs.getLong("id"));
            }
        }

        return ids;
    }

    public void deleteDeath(Connection conn, long id) throws SQLException {
        deleteDeathPS.setLong(1, id);
        deleteDeathPS.executeUpdate();
    }
}
