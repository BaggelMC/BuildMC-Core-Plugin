package net.mathias2246.buildmc;

import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.claims.protections.blocks.Beehive;
import net.mathias2246.buildmc.claims.protections.blocks.BoneMeal;
import net.mathias2246.buildmc.claims.protections.blocks.Break;
import net.mathias2246.buildmc.claims.protections.entities.ArmorStand;
import net.mathias2246.buildmc.claims.protections.misc.Buckets;
import net.mathias2246.buildmc.claims.protections.misc.EntityExplosionDamage;
import net.mathias2246.buildmc.claims.protections.misc.Explosion;
import net.mathias2246.buildmc.claims.protections.misc.PlayerFriendlyFire;
import net.mathias2246.buildmc.database.ClaimTable;
import net.mathias2246.buildmc.database.DatabaseConfig;
import net.mathias2246.buildmc.database.DatabaseManager;
import net.mathias2246.buildmc.event.claims.PlayerEnterClaimListener;
import net.mathias2246.buildmc.util.BStats;
import net.mathias2246.buildmc.util.SoundManager;
import net.mathias2246.buildmc.util.config.ConfigHandler;
import net.mathias2246.buildmc.util.config.ConfigurationValidationException;
import net.mathias2246.buildmc.util.language.LanguageManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.SQLException;

public final class CoreMain {

    public static Plugin plugin;

    public static MainClass mainClass;

    public static SoundManager soundManager;

    public static DatabaseConfig databaseConfig;

    public static DatabaseManager databaseManager;
    public static ClaimTable claimTable;

    private static boolean isInitialized = false;

    public static boolean isInitialized() {
        return isInitialized;
    }


    @ApiStatus.Internal
    public static void initialize(@NotNull Plugin plugin) {
        CoreMain.plugin = plugin;

        if (isInitialized) {
            plugin.getLogger().warning("CoreMain has been initialized multiple times.");
        }

        CoreMain.mainClass = (MainClass) plugin;

        initializeConfigs();

        BStats.initialize();

        LanguageManager.init();

        var config = plugin.getConfig();

        if (plugin.getConfig().getBoolean("claims.enabled")) {
            initializeDatabase();

            Protection.protections.addEntrys(
                    new Explosion(config.getConfigurationSection("claims.protections.damage.explosion-block-damage")),
                    new EntityExplosionDamage(config.getConfigurationSection("claims.protections.damage.explosion-entity-damage")),
                    new PlayerFriendlyFire(config.getConfigurationSection("claims.protections.damage.player-friendly-fire")),
                    new ArmorStand(config.getConfigurationSection("claims.protections.interactions.armor-stands")),
                    new Beehive(config.getConfigurationSection("claims.protections.interactions.beehives")),
                    new BoneMeal(config.getConfigurationSection("claims.protections.interactions.bone-meal")),
                    new Break(config.getConfigurationSection("claims.protections.player-break")),
                    new Buckets(config.getConfigurationSection("claims.protections.bucket-usage"))
            );

            Protection.protections.initialize();
            for (Protection protection : Protection.protections) {
                if (protection.isDefaultEnabled()) Protection.defaultProtections.add(protection.getKey().toString());
                registerEvent(protection);
            }

            registerEvent(new PlayerEnterClaimListener());
        }

        isInitialized = true;
    }

    public static void registerEvent(@NotNull Listener event) {
        plugin.getServer().getPluginManager().registerEvents(event, plugin);
    }

    @ApiStatus.Internal
    public static void stop() {
        if (plugin.getConfig().getBoolean("claims.enabled")) databaseManager.close();


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
            ClaimTable.calculateRemainingClaims(databaseManager.getConnection());
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
            plugin.getLogger().severe("Failed to load config: " + e);
        } catch (ConfigurationValidationException e) {
            plugin.getLogger().severe("Config validation failed: " + e);
        }
    }
}
