package net.mathias2246.buildmc.database;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PermissionsTable implements DatabaseTable {

    public PermissionsTable() {

    }

    private final Cache<@NotNull UUID, @NotNull HashSet<Permission>> permissionsCache = CacheBuilder.newBuilder()
            .maximumSize(128)
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .build();

    public void invalidateCache() { permissionsCache.invalidateAll(); }

    @Override
    public void createTable(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
            CREATE TABLE IF NOT EXISTS player_permissions
            (
                player_uuid       UUID NOT NULL,
                permission        VARCHAR(MAX),
                permission_state BOOLEAN
            );
            """);
        }
    }

    public void writePlayersPermissions(@NotNull Player player) {

    }
}
