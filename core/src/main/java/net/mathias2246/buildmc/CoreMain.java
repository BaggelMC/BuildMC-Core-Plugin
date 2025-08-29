package net.mathias2246.buildmc;

import net.mathias2246.buildmc.api.BuildMcAPI;
import net.mathias2246.buildmc.api.BuildMcAPIImpl;
import net.mathias2246.buildmc.api.event.BuildMcInitializedEvent;
import net.mathias2246.buildmc.claims.Protection;
import net.mathias2246.buildmc.claims.protections.misc.ExplosionProtection;
import net.mathias2246.buildmc.database.ClaimTable;
import net.mathias2246.buildmc.database.DatabaseConfig;
import net.mathias2246.buildmc.database.DatabaseManager;
import net.mathias2246.buildmc.event.claims.PlayerEnterClaimListener;
import net.mathias2246.buildmc.util.BStats;
import net.mathias2246.buildmc.util.SoundManager;
import net.mathias2246.buildmc.util.config.ConfigHandler;
import net.mathias2246.buildmc.util.config.ConfigurationValidationException;
import net.mathias2246.buildmc.util.language.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
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

        BuildMcAPI api = new BuildMcAPIImpl(plugin, mainClass);
        plugin.getServer().getServicesManager().register(BuildMcAPI.class, api, plugin, ServicePriority.High);

        initializeConfigs();

        BStats.initialize();

        LanguageManager.init();

        if (plugin.getConfig().getBoolean("claims.enabled")) {
            initializeDatabase();

            Protection.protections.addEntry(
                    new ExplosionProtection()
            );

            Protection.protections.initialize();
            for (Protection protection : Protection.protections) {
                if (protection.isDefaultEnabled()) Protection.defaultProtections.add(protection.getKey());
                registerEvent(protection);
            }

            registerEvent(new PlayerEnterClaimListener());

//            registerEvent(new ClaimContainerListener());
//            registerEvent(new ClaimExplosionsListener());
//            registerEvent(new ClaimBreakListener());
//            registerEvent(new ClaimPlaceListener());
//            registerEvent(new ClaimEntityPlaceListener());
//            registerEvent(new ClaimDamageProtectionListener());
//            registerEvent(new ClaimSignEditListener());
//            registerEvent(new ClaimInteractionListener());
//            registerEvent(new ClaimProjectileInteractListener());
//            registerEvent(new ClaimFishingListener());
//            registerEvent(new ClaimPotionSplashListener());
//            registerEvent(new ClaimVehicleEnterListener());
//            registerEvent(new ClaimBucketUseListener());
//            registerEvent(new ClaimEntityChangeBlockListener());
//            registerEvent(new ClaimItemPickupListener());
//            registerEvent(new ClaimItemDropListener());
//            registerEvent(new ClaimFrostWalkerListener());
//            registerEvent(new ClaimSculkSensorListener());
//            registerEvent(new ClaimPistonMovementListener());
//            registerEvent(new ClaimBeehiveInteractListener());
//            registerEvent(new ClaimBonemealInteractListener());
//            registerEvent(new ClaimEntityLeashListener());
//            registerEvent(new ClaimShearEntityListener());
//            registerEvent(new ClaimItemFrameRotateListener());
//            registerEvent(new ClaimNameTagUseListener());
//            registerEvent(new ClaimHangingInteractListener());
//            registerEvent(new ClaimArmorStandListener());
//            registerEvent(new ClaimEntityTameListener());
        }

        isInitialized = true;
        Bukkit.getPluginManager().callEvent(new BuildMcInitializedEvent(api));
    }

    public static void registerEvent(@NotNull Listener event) {
        plugin.getServer().getPluginManager().registerEvents(event, plugin);
    }

    @ApiStatus.Internal
    public static void stop() {
        if (plugin.getConfig().getBoolean("claims.enabled")) databaseManager.close();

        plugin.getServer().getServicesManager().unregister(BuildMcAPI.class, plugin);
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
