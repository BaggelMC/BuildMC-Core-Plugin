package net.mathias2246.buildmc.database;

import java.sql.*;

public class SchemaVersionTable implements DatabaseTable {
    public static final int INITIAL_VERSION = 1;

    @Override
    public void createTable(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS schema_version (version INT NOT NULL)");
        }

        try (PreparedStatement checkStmt = connection.prepareStatement("SELECT COUNT(*) FROM schema_version");
             ResultSet rs = checkStmt.executeQuery()) {
            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement insertStmt = connection.prepareStatement(
                        "INSERT INTO schema_version (version) VALUES (?)")) {
                    insertStmt.setInt(1, INITIAL_VERSION);
                    insertStmt.executeUpdate();
                }
            }
        }
    }

    public int getCurrentVersion(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT version FROM schema_version")) {
            if (rs.next()) {
                return rs.getInt("version");
            }
            return INITIAL_VERSION;
        }
    }

    public void updateVersion(Connection connection, int newVersion) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE schema_version SET version = ?")) {
            stmt.setInt(1, newVersion);
            stmt.executeUpdate();
        }
    }
}
