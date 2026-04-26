package net.mathias2246.buildmc.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class PermissionsTable implements DatabaseTable {
    @Override
    public void createTable(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS permission_groups (
                    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name    VARCHAR(64) NOT NULL UNIQUE
                );
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS group_permissions (
                    group_id    BIGINT      NOT NULL,
                    permission  VARCHAR(256) NOT NULL,
                    granted     BOOLEAN     NOT NULL DEFAULT TRUE,
                    PRIMARY KEY (group_id, permission),
                    FOREIGN KEY (group_id) REFERENCES permission_groups(id) ON DELETE CASCADE
                );
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS group_children (
                    parent_id   BIGINT NOT NULL,
                    child_id    BIGINT NOT NULL,
                    PRIMARY KEY (parent_id, child_id),
                    FOREIGN KEY (parent_id) REFERENCES permission_groups(id) ON DELETE CASCADE,
                    FOREIGN KEY (child_id)  REFERENCES permission_groups(id) ON DELETE CASCADE
                );
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS player_groups (
                    player_uuid VARCHAR(36) NOT NULL,
                    group_id    BIGINT      NOT NULL,
                    PRIMARY KEY (player_uuid, group_id),
                    FOREIGN KEY (group_id) REFERENCES permission_groups(id) ON DELETE CASCADE
                );
            """);
        }
    }
}
