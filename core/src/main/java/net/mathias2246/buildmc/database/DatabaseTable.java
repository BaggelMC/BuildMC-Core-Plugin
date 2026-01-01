package net.mathias2246.buildmc.database;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface DatabaseTable {
    void createTable(Connection connection) throws SQLException;
}
