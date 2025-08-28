package net.mathias2246.buildmc.database;

import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.database.migrations.Migration;
import net.mathias2246.buildmc.database.migrations.MigrationV1;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DatabaseManager {

    private final DatabaseConfig config;
    private Connection connection;
    private final Logger logger;
    private final File databaseFolder;
    private final List<DatabaseTable> registeredTables = new ArrayList<>();

    private final List<Migration> migrations = List.of(
            new MigrationV1()
    );

    public DatabaseManager(Plugin plugin) {
        this.config = CoreMain.databaseConfig;
        this.logger = plugin.getLogger();
        this.databaseFolder = new File(plugin.getDataFolder(), "Data"); // Use /Data folder

        if (!databaseFolder.exists()) {
            if (databaseFolder.mkdirs()) {
                logger.info("Created database directory: " + databaseFolder.getAbsolutePath());
            } else {
                logger.severe("Failed to create database directory!");
            }
        }

        config.reloadConfig(); // Ensure the latest config is loaded
    }

    public void connect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.warning("Error closing old database connection: " + e);
        }

        String databaseFilePath = new File(databaseFolder, "database").getAbsolutePath();
        String url = config.isServerMode() ? config.getServerUrl() : "jdbc:h2:file:" + databaseFilePath;

        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection(url, "sa", "");
        } catch (ClassNotFoundException | SQLException e) {
            logger.severe("Database connection failed: " + e);
        }

        runMigrations();
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            logger.severe("Failed to check or re-establish database connection: " + e);
        }
        return connection;
    }

    public void runMigrations() {
        try {
            SchemaVersionTable versionTable = new SchemaVersionTable();
            versionTable.createTable(getConnection()); // Ensure version table exists

            int currentVersion = versionTable.getCurrentVersion(getConnection());

            for (Migration migration : migrations) {
                if (migration.getTargetVersion() > currentVersion) {
                    logger.info("Applying database migration to version " + migration.getTargetVersion());
                    migration.migrate(getConnection());
                    versionTable.updateVersion(getConnection(), migration.getTargetVersion());
                    logger.info("Database migration to version " + migration.getTargetVersion() + " applied.");
                }
            }

        } catch (SQLException e) {
            logger.severe("Migration failed: " + e);
            throw new RuntimeException("Database migration failed", e);
        }
    }

    public void registerTable(DatabaseTable table) {
        registeredTables.add(table);
        try {
            table.createTable(getConnection());
        } catch (SQLException e) {
            logger.severe("Failed to create table: " + table.getClass().getSimpleName() + " - " + e);
        }
    }

    public List<DatabaseTable> getRegisteredTables() {
        return registeredTables;
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
                logger.info("Database connection closed.");
            } catch (SQLException e) {
                logger.severe("Error closing database connection: " + e);
            }
        }
    }
}
