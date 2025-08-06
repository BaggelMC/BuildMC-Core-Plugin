package net.mathias2246.buildmc;

import net.mathias2246.buildmc.database.ClaimTable;
import net.mathias2246.buildmc.database.DatabaseConfig;
import net.mathias2246.buildmc.database.DatabaseManager;
import net.mathias2246.buildmc.util.config.ConfigHandler;
import net.mathias2246.buildmc.util.config.ConfigurationValidationException;
import net.mathias2246.buildmc.util.language.LanguageManager;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.SQLException;

public final class CoreMain {

    public static Plugin plugin;

    public static MainClass mainClass;

    public static DatabaseConfig databaseConfig;

    public static DatabaseManager databaseManager;
    public static ClaimTable claimTable;

    public static void initialize(@NotNull Plugin plugin) {
        CoreMain.plugin = plugin;

        CoreMain.mainClass = (MainClass) plugin;

        initializeConfigs();

        LanguageManager.init();

        initializeDatabase();
    }

    public static void stop() {
        databaseManager.close();
    }

    private static void initializeConfigs() {
        databaseConfig = new DatabaseConfig();
        initConfig(databaseConfig);
    }

    private static void initializeDatabase() {
        databaseManager = new DatabaseManager(CoreMain.plugin);
        claimTable = new ClaimTable();
        databaseManager.registerTable(claimTable);

        try {
            claimTable.loadClaimOwners(databaseManager.getConnection());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void initConfig(ConfigHandler config) {
        config.generateConfig();
        try {
            config.loadConfig();
            config.validateConfig();
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load config: " + e.getMessage());
        } catch (ConfigurationValidationException e) {
            plugin.getLogger().severe("Config validation failed: " + e.getMessage());
        }
    }
}
