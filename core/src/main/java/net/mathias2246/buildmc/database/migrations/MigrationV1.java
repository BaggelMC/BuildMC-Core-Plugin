package net.mathias2246.buildmc.database.migrations;

import java.sql.Connection;
import java.sql.SQLException;

/**The first version of our internal database formats.*/
public class MigrationV1 implements Migration {

    @Override
    public int getTargetVersion() {
        return 1;
    }

    @Override
    public void migrate(Connection connection) throws SQLException {

    }
}
