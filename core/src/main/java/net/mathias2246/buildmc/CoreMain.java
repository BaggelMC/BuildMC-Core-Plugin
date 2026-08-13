package net.mathias2246.buildmc;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.mathias2246.buildmc.api.BuildMcAPI;
import net.mathias2246.buildmc.api.Guide;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.api.event.lifecycle.BuildMcFinishedLoadingEvent;
import net.mathias2246.buildmc.api.event.lifecycle.BuildMcRegistryEvent;
import net.mathias2246.buildmc.api.item.AbstractCustomItem;
import net.mathias2246.buildmc.api.item.ItemDropTracker;
import net.mathias2246.buildmc.api.messageBus.EventChannel;
import net.mathias2246.buildmc.api.messageBus.EventSubscription;
import net.mathias2246.buildmc.api.status.StatusInstance;
import net.mathias2246.buildmc.api.status.StatusManager;
import net.mathias2246.buildmc.claims.ClaimLogger;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.claims.protections.ProtectionConfig;
import net.mathias2246.buildmc.commands.GuidesCommand;
import net.mathias2246.buildmc.database.ClaimTable;
import net.mathias2246.buildmc.database.DatabaseConfig;
import net.mathias2246.buildmc.database.DatabaseManager;
import net.mathias2246.buildmc.database.DeathTable;
import net.mathias2246.buildmc.deaths.DeathListener;
import net.mathias2246.buildmc.messageBus.EventBusImpl;
import net.mathias2246.buildmc.player.FirstTimeJoinListener;
import net.mathias2246.buildmc.player.FirstTimeJoinMessage;
import net.mathias2246.buildmc.player.status.PlayerStatusUtil;
import net.mathias2246.buildmc.player.status.StatusConfig;
import net.mathias2246.buildmc.team.TeamManager;
import net.mathias2246.buildmc.util.AnnotatedRegistry;
import net.mathias2246.buildmc.util.BStats;
import net.mathias2246.buildmc.util.SoundManager;
import net.mathias2246.buildmc.util.language.LanguageManager;
import net.mathias2246.buildmc.util.registry.BaseRegistry;
import net.mathias2246.buildmc.util.registry.DefaultRegistries;
import net.mathias2246.buildmc.util.registry.DeferredRegistry;
import net.mathias2246.buildmc.util.registry.RegistriesHolder;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.ServicePriority;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.SQLException;

import static net.mathias2246.buildmc.commands.disabledCommands.DisableCommands.disableCommand;
import static net.mathias2246.buildmc.player.TeleportTimer.cooldown;

@ApiStatus.Internal
public final class CoreMain {

    @Subst("")
    public static PluginMain plugin;
    public static FileConfiguration config;
    public static File configFile;

    public static SoundManager soundManager;

    public static DatabaseConfig databaseConfig;

    public static DatabaseManager databaseManager;
    public static ClaimTable claimTable;
    public static DeathTable deathTable;

    private static boolean isInitialized = false;

    public static boolean isInitialized() { return isInitialized; }

    public static final RegistriesHolder registriesHolder = new RegistriesHolder.Builder().build();

    public static DeferredRegistry<Protection> protectionsRegistry;

    public static BaseRegistry<StatusInstance> statusesRegistry;

    public static DeferredRegistry<AbstractCustomItem> customItemsRegistry;

    public static DeferredRegistry<EventChannel> channelRegistry;

    public static DeferredRegistry<EventSubscription> subscriptionRegistry;

    public static BaseRegistry<Guide> guidesRegistry;

    public final static Gson gson = new GsonBuilder()
            .serializeNulls()
            .registerTypeAdapter(StatusInstance.class, new StatusConfig.StatusInstanceJsonDeserializer())
            .create();

    public final static GsonComponentSerializer gsonComponentSerializer = GsonComponentSerializer.builder().build();

    public static GuidesCommand guideCommand;

    public static StatusManager statusManager;

    public static EventBusImpl eventBus;

    public static TeamManager teamManager;

    public static boolean isDiscordExtensionInstalled = false;
    public static final @NotNull NamespacedKey discordExtensionChannelKey =
            new NamespacedKey("buildmc", "discord_webhook");

    @ApiStatus.Internal
    public static void initialize(@NotNull PluginMain plugin) {
        if (isInitialized) {
            plugin.getLogger().warning("CoreMain has been initialized multiple times!");
        }

        CoreMain.plugin = plugin;

        initializeConfigs();

        BStats.initialize();

        LanguageManager.init();

        config = plugin.getConfig();

        Bukkit.getServicesManager().register(BuildMcAPI.class, plugin, plugin, ServicePriority.High);

        guidesRegistry = (BaseRegistry<Guide>) registriesHolder.addRegistry(DefaultRegistries.GUIDES.toString(), new BaseRegistry<Guide>(Key.key("buildmc", "guide")));

        guideCommand = new GuidesCommand(plugin, "guides.yml");
        GuidesCommand.enabled = guideCommand.configuration.getBoolean("enabled", true);

        statusesRegistry = (BaseRegistry<StatusInstance>) registriesHolder.addRegistry(DefaultRegistries.STATUSES.toString(), new BaseRegistry<StatusInstance>(Key.key("buildmc", "status")));

        protectionsRegistry = (DeferredRegistry<Protection>) registriesHolder.addRegistry(DefaultRegistries.PROTECTIONS.toString(), new DeferredRegistry<Protection>(Key.key("buildmc", "protection")));

        customItemsRegistry = (DeferredRegistry<AbstractCustomItem>) registriesHolder.addRegistry(DefaultRegistries.CUSTOM_ITEMS.toString(), AbstractCustomItem.customItemsRegistry);

        protectionsRegistry.addEntries(
                plugin,
                AnnotatedRegistry.loadSafe(
                        config,
                        "net.mathias2246.buildmc.claims.protections",
                        Protection.class,
                        ProtectionConfig.class,
                        info -> (String) info.getParameterValues().getValue("value"),
                        info -> (Boolean) info.getParameterValues().getValue("defaultEnabled")
                )
        );
        plugin.getLogger().info("Initialized " + protectionsRegistry.size() + " protections in core!");

        channelRegistry = new DeferredRegistry<>(Key.key("buildmc", "event_channels"));
        registriesHolder.addRegistry(DefaultRegistries.EVENT_CHANNELS.toString(), channelRegistry);
        subscriptionRegistry = new DeferredRegistry<>(Key.key("buildmc", "event_subscriptions"));
        registriesHolder.addRegistry(DefaultRegistries.EVENT_SUBSCRIPTIONS.toString(), subscriptionRegistry);

        eventBus = new EventBusImpl(channelRegistry, subscriptionRegistry, plugin.getLogger());

        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onServerLoad(ServerLoadEvent event) {
                Bukkit.getPluginManager().callEvent(new BuildMcRegistryEvent(plugin));
                finishLoading();
            }
        }, plugin);
    }

    public static void finishLoading() {
        channelRegistry.initialize();
        subscriptionRegistry.initialize();
        eventBus.initialize();

        cooldown = CoreMain.plugin.getConfig().getInt("spawn-teleport.cooldown", 120);
        if (cooldown < 0) cooldown = null;

        initializeDatabase();

        if (channelRegistry.get(discordExtensionChannelKey) != null) {
            isDiscordExtensionInstalled = true;
        }

        if (plugin.getConfig().getBoolean("claims.enabled", true)) {
            ClaimManager.initializeClaimSystem();
        }

        registerListener(new FirstTimeJoinListener());
        if (config.getBoolean("first-time-join.show-first-join-message")) registerListener(new FirstTimeJoinMessage());

        registerListener(new DeathListener());

        if (config.getBoolean("status.enabled")) {
            registerListener(new PlayerStatusUtil());
        }

        if (config.getBoolean("disable-commands")) {
            if (config.isList("disabled-commands")) {
                for (String fullCommand : config.getStringList("disabled-commands")) {
                    String[] parts = fullCommand.split(":", 2);
                    if (parts.length == 2) {
                        disableCommand(parts[0], parts[1]);
                    } else {
                        plugin.getLogger().warning("Invalid command format in 'disabled-commands': " + fullCommand);
                    }
                }
            }
        }

        customItemsRegistry.initialize();

        new ItemDropTracker(plugin);

        plugin.finishLoading();

        isInitialized = true;

        Bukkit.getPluginManager().callEvent(new BuildMcFinishedLoadingEvent(plugin));

    }

    public static void registerListener(@NotNull Listener event) {
        plugin.getServer().getPluginManager().registerEvents(event, plugin);
    }

    public static ProtocolManager protocolManager;

    public static void onLoad() {
        protocolManager = ProtocolLibrary.getProtocolManager();
    }

    @SuppressWarnings("EmptyMethod")
    @ApiStatus.Internal
    public static void stop() {
        databaseManager.close();
        ClaimLogger.shutdown();
    }

    private static void initializeConfigs() {
        databaseConfig = new DatabaseConfig();
    }

    private static void initializeDatabase() {
        databaseManager = new DatabaseManager(CoreMain.plugin);

        claimTable = new ClaimTable();
        databaseManager.registerTable(claimTable);

        deathTable = new DeathTable();
        databaseManager.registerTable(deathTable);

        teamManager = new net.mathias2246.buildmc.team.TeamManager();
        databaseManager.registerTable(teamManager);

        try {
            teamManager.loadAllTeams(databaseManager.getConnection());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            claimTable.loadClaimOwners(databaseManager.getConnection());
            ClaimTable.calculateRemainingClaims(databaseManager.getConnection());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
