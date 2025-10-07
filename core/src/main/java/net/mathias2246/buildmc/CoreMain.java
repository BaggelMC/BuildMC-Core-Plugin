package net.mathias2246.buildmc;

import net.mathias2246.buildmc.api.BuildMcAPI;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.api.event.lifecycle.BuildMcFinishedLoadingEvent;
import net.mathias2246.buildmc.api.event.lifecycle.BuildMcRegistryEvent;
import net.mathias2246.buildmc.claims.protections.blocks.*;
import net.mathias2246.buildmc.claims.protections.entities.*;
import net.mathias2246.buildmc.claims.protections.misc.*;
import net.mathias2246.buildmc.database.ClaimTable;
import net.mathias2246.buildmc.database.DatabaseConfig;
import net.mathias2246.buildmc.database.DatabaseManager;
import net.mathias2246.buildmc.event.claims.PlayerCrossClaimBoundariesListener;
import net.mathias2246.buildmc.util.*;
import net.mathias2246.buildmc.util.config.ConfigHandler;
import net.mathias2246.buildmc.util.config.ConfigurationValidationException;
import net.mathias2246.buildmc.util.language.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.SQLException;

@ApiStatus.Internal
public final class CoreMain {

    public static Plugin plugin;
    public static MainClass mainClass;
    public static PluginMain pluginMain;
    public static BuildMcAPI api;

    public static SoundManager soundManager;

    public static DatabaseConfig databaseConfig;

    public static DatabaseManager databaseManager;
    public static ClaimTable claimTable;

    private static boolean isInitialized = false;

    public static final RegistriesHolder registriesHolder = new RegistriesHolder.Builder().build();

    public static DeferredRegistry<Protection> protectionsRegistry;

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

        BStats.initialize();

        LanguageManager.init();

        var config = plugin.getConfig();

        protectionsRegistry = registriesHolder.addRegistry(DefaultRegistries.PROTECTIONS.toString(), new DeferredRegistry<>());


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

            for (Protection protection : protectionsRegistry) {
                var def = protection.isDefaultEnabled();

                // Don't register protection events if, they are disabled and cannot be changed
                if (!def && CoreMain.plugin.getConfig().getBoolean("claims.hide-all-protections")) continue;

                if (def) Protection.defaultProtections.add(protection.getKey().toString());
                registerEvent(protection);
            }

            registerEvent(new PlayerCrossClaimBoundariesListener());
        }

        pluginMain.finishLoading();

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
