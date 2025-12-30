package net.mathias2246.buildmc.database;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseTable {
    void createTable(Connection connection) throws SQLException;

    void prepareStatements(Connection connection) throws SQLException;

    void closeStatements(Connection connection) throws SQLException;
}
