package net.mathias2246.buildmc.database.migrations;

import java.sql.Connection;
import java.sql.SQLException;

public interface Migration {
    int getTargetVersion();
    void migrate(Connection connection) throws SQLException;
}
