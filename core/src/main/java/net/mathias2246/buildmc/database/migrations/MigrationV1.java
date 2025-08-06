package net.mathias2246.buildmc.database.migrations;

import java.sql.Connection;
import java.sql.SQLException;

public class MigrationV1 implements Migration {

    @Override
    public int getTargetVersion() {
        return 1;
    }

    @Override
    public void migrate(Connection connection) throws SQLException {
//        try (Statement stmt = connection.createStatement()) {
//            stmt.execute("CREATE TABLE example_table (id INT PRIMARY KEY, name VARCHAR(255))");
//        }
    }
}
