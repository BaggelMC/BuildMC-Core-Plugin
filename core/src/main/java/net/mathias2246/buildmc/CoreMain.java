package net.mathias2246.buildmc;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.mathias2246.buildmc.api.BuildMcAPI;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.api.event.lifecycle.BuildMcFinishedLoadingEvent;
import net.mathias2246.buildmc.api.event.lifecycle.BuildMcRegistryEvent;
import net.mathias2246.buildmc.api.item.AbstractCustomItem;
import net.mathias2246.buildmc.api.item.ItemDropTracker;
import net.mathias2246.buildmc.api.permissions.PermissionGroup;
import net.mathias2246.buildmc.api.status.StatusInstance;
import net.mathias2246.buildmc.api.status.StatusManager;
import net.mathias2246.buildmc.claims.ClaimLogger;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.claims.protections.blocks.*;
import net.mathias2246.buildmc.claims.protections.entities.*;
import net.mathias2246.buildmc.claims.protections.misc.*;
import net.mathias2246.buildmc.database.ClaimTable;
import net.mathias2246.buildmc.database.DatabaseConfig;
import net.mathias2246.buildmc.database.DatabaseManager;
import net.mathias2246.buildmc.event.claims.PlayerCrossClaimBoundariesListener;
import net.mathias2246.buildmc.permissions.PermissionGroupLoader;
import net.mathias2246.buildmc.util.BStats;
import net.mathias2246.buildmc.util.SoundManager;
import net.mathias2246.buildmc.util.config.ConfigHandler;
import net.mathias2246.buildmc.util.config.ConfigurationValidationException;
import net.mathias2246.buildmc.util.language.LanguageManager;
import net.mathias2246.buildmc.util.registry.BaseRegistry;
import net.mathias2246.buildmc.util.registry.DefaultRegistries;
import net.mathias2246.buildmc.util.registry.DeferredRegistry;
import net.mathias2246.buildmc.util.registry.RegistriesHolder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.Plugin;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@ApiStatus.Internal
public final class CoreMain {

    @Subst("")
    public static Plugin plugin;
    public static MainClass mainClass;
    public static PluginMain pluginMain;
    public static BuildMcAPI api;
    public static FileConfiguration config;
    public static File configFile;

    public static SoundManager soundManager;

    public static DatabaseConfig databaseConfig;

    public static DatabaseManager databaseManager;
    public static ClaimTable claimTable;

    public static BukkitAudiences bukkitAudiences;

    private static boolean isInitialized = false;

    public static final RegistriesHolder registriesHolder = new RegistriesHolder.Builder().build();

    public static DeferredRegistry<Protection> protectionsRegistry;

    public static BaseRegistry<StatusInstance> statusesRegistry;

    public static DeferredRegistry<AbstractCustomItem> customItemsRegistry;

    public static DeferredRegistry<PermissionGroup> permissionGroupRegistry;

    public static StatusManager statusManager;

    public static boolean isInitialized() {
        return isInitialized;
    }

    @ApiStatus.Internal
    public static void initialize(@NotNull PluginMain plugin) {
        if (isInitialized) {
            plugin.getLogger().warning("CoreMain has been initialized multiple times!");
        }

        // Bruh
        CoreMain.plugin = plugin;
        CoreMain.mainClass = plugin;
        CoreMain.pluginMain = plugin;
        CoreMain.api = plugin;

        initializeConfigs();

        bukkitAudiences = BukkitAudiences.create(plugin);

        BStats.initialize();

        LanguageManager.init();

        config = plugin.getConfig();

        statusesRegistry = (BaseRegistry<StatusInstance>) registriesHolder.addRegistry(DefaultRegistries.STATUSES.toString(), new BaseRegistry<StatusInstance>());

        protectionsRegistry = (DeferredRegistry<Protection>) registriesHolder.addRegistry(DefaultRegistries.PROTECTIONS.toString(), new DeferredRegistry<Protection>());

        customItemsRegistry = (DeferredRegistry<AbstractCustomItem>) registriesHolder.addRegistry(DefaultRegistries.CUSTOM_ITEMS.toString(), AbstractCustomItem.customItemsRegistry);

        permissionGroupRegistry = (DeferredRegistry<PermissionGroup>) registriesHolder.addRegistry(DefaultRegistries.PERMISSION_GROUPS.toString(), new DeferredRegistry<PermissionGroup>());

        new PermissionGroupLoader(permissionGroupRegistry).loadAll();

        protectionsRegistry.addEntries(
                new Explosion(config.getConfigurationSection("claims.protections.damage.explosion-block-damage")),
                new EntityExplosionDamage(config.getConfigurationSection("claims.protections.damage.explosion-entity-damage")),
                new PlayerFriendlyFire(config.getConfigurationSection("claims.protections.damage.player-friendly-fire")),
                new ArmorStand(config.getConfigurationSection("claims.protections.interactions.armor-stands")),
                new Beehive(config.getConfigurationSection("claims.protections.interactions.beehives")),
                new BoneMeal(config.getConfigurationSection("claims.protections.interactions.bone-meal")),
                new Break(config.getConfigurationSection("claims.protections.player-break")),
                new Buckets(config.getConfigurationSection("claims.protections.bucket-usage")),
                new Containers(config.getConfigurationSection("claims.protections.container")),
                new EntityDamage(config.getConfigurationSection("claims.protections.damage.entity-damage")),
                new EntityBlockModifications(config.getConfigurationSection("claims.protections.entity-modifications")),
                new EntityLeash(config.getConfigurationSection("claims.protections.interactions.attach-leash")),
                new EntityPlace(config.getConfigurationSection("claims.protections.player-place-entity")),
                new EntityTame(config.getConfigurationSection("claims.protections.interactions.tame-entity")),
                new Fishing(config.getConfigurationSection("claims.protections.fishing")),
                new FrostWalker(config.getConfigurationSection("claims.protections.frostwalker")),
                new HangingEntities(config.getConfigurationSection("claims.protections.hanging-entities")),
                new DoorInteractions(config.getConfigurationSection("claims.protections.interactions.doors")),
                new ButtonAndLever(config.getConfigurationSection("claims.protections.interactions.buttons-and-levers")),
                new RedstoneComponents(config.getConfigurationSection("claims.protections.interactions.redstone-components")),
                new LightTNT(config.getConfigurationSection("claims.protections.interactions.light-tnt")),
                new PressurePlatesAndTripwires(config.getConfigurationSection("claims.protections.interactions.pressure-plates-and-tripwire")),
                new Candles(config.getConfigurationSection("claims.protections.interactions.candles")),
                new Farmland(config.getConfigurationSection("claims.protections.interactions.farmland")),
                new Jukebox(config.getConfigurationSection("claims.protections.interactions.jukebox")),
                new Campfire(config.getConfigurationSection("claims.protections.interactions.campfire")),
                new Bells(config.getConfigurationSection("claims.protections.interactions.bells")),
                new ItemFrameInteractions(config.getConfigurationSection("claims.protections.interactions.item-frame")),
                new ItemDrop(config.getConfigurationSection("claims.protections.item-drop")),
                new ItemPickup(config.getConfigurationSection("claims.protections.item-pickup")),
                new EntityNameTag(config.getConfigurationSection("claims.protections.interactions.name-tag")),
                new Place(config.getConfigurationSection("claims.protections.player-place")),
                new PotionSplash(config.getConfigurationSection("claims.protections.splash-potions")),
                new ProjectileInteractions(config.getConfigurationSection("claims.protections.projectile-interactions")),
                new SculkSensors(config.getConfigurationSection("claims.protections.sculk-sensor")),
                new EntityShear(config.getConfigurationSection("claims.protections.interactions.shear-entity")),
                new SignEdit(config.getConfigurationSection("claims.protections.sign-editing")),
                new VehicleEnter(config.getConfigurationSection("claims.protections.vehicle-enter")),
                new PistonMovement(config.getConfigurationSection("claims.protections.piston-movement-across-claim-borders"))
                );

        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onServerLoad(ServerLoadEvent event) {
                Bukkit.getPluginManager().callEvent(new BuildMcRegistryEvent(api));
                finishLoading();
            }
        }, plugin);
    }

    public static void finishLoading() {
        if (plugin.getConfig().getBoolean("claims.enabled", true)) {
            initializeDatabase();

            var dimensionListSect = config.getConfigurationSection("claims.dimensions");
            if (dimensionListSect != null) {
                ClaimManager.isDimensionBlacklist = dimensionListSect.getBoolean("blacklist", true);
                List<String> str = dimensionListSect.getStringList("list");
                for (World w : Bukkit.getWorlds()) {
                    if (str.contains(w.getKey().toString())) ClaimManager.dimensionList.add(w);
                }
            }

            var hideAllProtections = CoreMain.plugin.getConfig().getBoolean("claims.hide-all-protections");
            for (Protection protection : protectionsRegistry) {
                var def = protection.isDefaultEnabled();

                // Register the protection's event listener
                registerEvent(protection);

                // Don't register protection events if, they are disabled and cannot be changed
                if (hideAllProtections) {
                    protection.setHidden(true);
                }

                // Add to the default protection list if applicable
                if (def) Protection.defaultProtections.add(protection.getKey().toString());
            }

            registerEvent(new PlayerCrossClaimBoundariesListener());

            ClaimLogger.init(plugin);
        }

        new ItemDropTracker(plugin);

        pluginMain.finishLoading();

        // Initialize deferred registries
        if (plugin.getConfig().getBoolean("claims.enabled", true)) {
            protectionsRegistry.initialize();
        }
        customItemsRegistry.initialize();
        permissionGroupRegistry.initialize();

        isInitialized = true;

        Bukkit.getPluginManager().callEvent(new BuildMcFinishedLoadingEvent(api));
    }

    public static void registerEvent(@NotNull Listener event) {
        plugin.getServer().getPluginManager().registerEvents(event, plugin);
    }

    @ApiStatus.Internal
    public static void stop() {
        if (plugin.getConfig().getBoolean("claims.enabled", true)) databaseManager.close();


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
