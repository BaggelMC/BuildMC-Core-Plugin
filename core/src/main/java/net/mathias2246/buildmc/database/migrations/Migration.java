package net.mathias2246.buildmc.database.migrations;

import java.sql.Connection;
import java.sql.SQLException;

/**This is used to migrate between different internal database formats.
 * <p>Used, for example, when adding new features to something that is stored inside a database.</p>*/
public interface Migration {
    int getTargetVersion();
    void migrate(Connection connection) throws SQLException;
}
