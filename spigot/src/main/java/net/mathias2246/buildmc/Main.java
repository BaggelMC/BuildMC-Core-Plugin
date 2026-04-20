package net.mathias2246.buildmc;

import dev.jorel.commandapi.CommandAPI;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.api.claims.ClaimManager;
import net.mathias2246.buildmc.api.endEvent.EndManager;
import net.mathias2246.buildmc.api.item.CustomItemListener;
import net.mathias2246.buildmc.api.spawnEyltra.ElytraManager;
import net.mathias2246.buildmc.api.status.StatusManager;
import net.mathias2246.buildmc.claims.ClaimCommand;
import net.mathias2246.buildmc.claims.ClaimManagerImpl;
import net.mathias2246.buildmc.claims.tool.ClaimToolParticles;
import net.mathias2246.buildmc.claims.tools.ClaimSelectionTool;
import net.mathias2246.buildmc.commands.*;
import net.mathias2246.buildmc.endEvent.EndListener;
import net.mathias2246.buildmc.endEvent.EndManagerImpl;
import net.mathias2246.buildmc.platform.BetterBungeeComponentSerializer;
import net.mathias2246.buildmc.platform.SoundManagerSpigotImpl;
import net.mathias2246.buildmc.player.PlayerHeadDropDeathListener;
import net.mathias2246.buildmc.player.PlayerHeadDropModifier;
import net.mathias2246.buildmc.player.PlayerSpawnTeleportCommand;
import net.mathias2246.buildmc.player.status.PlayerStatus;
import net.mathias2246.buildmc.player.status.SetStatusCommand;
import net.mathias2246.buildmc.spawnElytra.*;
import net.mathias2246.buildmc.status.StatusConfig;
import net.mathias2246.buildmc.util.Message;
import net.mathias2246.buildmc.util.SoundManager;
import net.mathias2246.buildmc.util.SoundUtil;
import net.mathias2246.buildmc.util.config.ConfigurationValidationException;
import net.mathias2246.buildmc.util.registry.RegistriesHolder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static net.mathias2246.buildmc.CoreMain.registriesHolder;

public final class Main extends PluginMain {

    public static Logger logger;

    public static PluginMain plugin;

    @Subst("")
    public static FileConfiguration config;
    public static File configFile;

    public static File pluginFolder;

    private static final ElytraZoneManager zoneManager = new ElytraZoneManager();

    public static StatusConfig statusConfig;

    public static BukkitAudiences audiences;

    public static ClaimManager apiClaimManager;
    public static EndManager apiEndManager;
    private static ElytraManager apiElytraManager;

    private boolean skippedLoad = false;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        logger = this.getLogger();

        if (isPaper()) {
            //noinspection ExtractMethodRecommender
            var t = new TextComponent("""
                            
                            ===========================================================
                                 You cannot use the Spigot version of BuildMC-Core
                                               on a Paper server!
                                       Please download the paper version:
                                    https://modrinth.com/plugin/buildmc-core
                            ===========================================================
                            """
            );
            t.setColor(ChatColor.RED);
            Bukkit.getConsoleSender().spigot().sendMessage(t);
            skippedLoad = true;
            Bukkit.getPluginManager().disablePlugin(this);

            return;
        }

        pluginFolder = plugin.getDataFolder();
        if (!pluginFolder.exists()) {
            //noinspection ResultOfMethodCallIgnored
            pluginFolder.mkdir();
        }

        configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) this.saveResource("config.yml", false);
        config = this.getConfig();

        CoreMain.configFile = configFile;

        CoreMain.initialize(this);

        if (config.getBoolean("claims.enabled", true)) {
            CoreMain.customItemsRegistry.addEntries(
                    new ClaimSelectionTool(this,
                            Objects.requireNonNull(NamespacedKey.fromString("buildmc:claim_tool")),
                            new ClaimToolParticles.Builder()
                    )
            );
        }

        CommandRegister.setupCommandAPI();

        audiences = BukkitAudiences.create(plugin);

        CoreMain.soundManager = new SoundManagerSpigotImpl();

        apiClaimManager = new ClaimManagerImpl();
        apiEndManager = new EndManagerImpl();
        apiElytraManager = new ElytraManagerImpl(zoneManager);
    }

    @Override
    public void onLoad() {
        CoreMain.onLoad();
    }

    @Override
    public void onDisable() {
        if (skippedLoad) return;
        // Plugin shutdown logic
        audiences.close();
        CommandAPI.onDisable();

        CoreMain.stop();
    }

    private void registerEvent(@NotNull Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    @Override
    public void sendMessage(CommandSender sender, Component message) {
        BaseComponent component = BetterBungeeComponentSerializer.serialize(message, Message.getLocale(sender));
        sender.spigot().sendMessage(component);
    }

    @Override
    public void sendPlayerActionBar(Player player, Component message) {
        BaseComponent component = BetterBungeeComponentSerializer.serialize(message, Message.getLocale(player));
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return this;
    }

    @Override
    public void editConfiguration(@NotNull Consumer<FileConfiguration> consumer) {
        consumer.accept(config);
    }

    @Override
    public @NotNull MainClass getMainClass() {
        return this;
    }

    @Override
    public @NotNull SoundManager getSoundManager() {
        return CoreMain.soundManager;
    }

    @Override
    public ClaimManager getClaimManager() {
        return apiClaimManager;
    }

    @Override
    public EndManager getEndManager() {
        return apiEndManager;
    }

    @Override
    public ElytraManager getElytraManager() {
        return apiElytraManager;
    }

    @Override
    public @NotNull StatusManager getStatusManager() {
        return CoreMain.statusManager;
    }

    @Override
    public @NotNull RegistriesHolder getRegistriesHolder() {
        return registriesHolder;
    }

    @Override
    public void finishLoading() {

        SoundUtil.setup();

        getServer().getPluginManager().registerEvents(new CustomItemListener(), this);

        try {
            EndListener.loadFromConfig();
        } catch (ConfigurationValidationException e) {
            throw new RuntimeException(e);
        }

        CommandRegister.register(new BuildMcCommand());
        CommandRegister.register(new BroadcastCommandPlatform());
        CommandRegister.register(new RulesCommandPLatform());
        CommandRegister.register(new DeathsCommandPlatform());

        getServer().getPluginManager().registerEvents(new EndListener(), this);

        if (config.getBoolean("spawn-elytra.enabled")) {
            getServer().getPluginManager().registerEvents(new SpawnBoostListener(zoneManager), this);
            if (config.getBoolean("spawn-elytra.disable-rockets")) getServer().getPluginManager().registerEvents(new DisableRocketListener(), this);
            getServer().getPluginManager().registerEvents(new ElytraCheckListeners(zoneManager, config.getBoolean("spawn-elytra.enabled", true), config.getDouble("spawn-elytra.strength", 2)), this);
            CommandRegister.register(new ElytraZoneCommand(zoneManager));
            zoneManager.loadZoneFromConfig();
        }

        if (config.getBoolean("claims.enabled", true)) {
            CommandRegister.register(new ClaimCommand());
        }

        if (GuidesCommand.enabled) {
            CommandRegister.register(new GuideCommand());
        }

        if (config.getBoolean("spawn-teleport.enabled", true)) {
            CommandRegister.register(new PlayerSpawnTeleportCommand());
        }

        if (config.getBoolean("status.enabled")) {
            statusConfig = new StatusConfig(this);
            CoreMain.statusManager = new PlayerStatus();
            CommandRegister.register(new SetStatusCommand(statusConfig));
        }

        if (config.getBoolean("player-head.on-death")) {
            getServer().getPluginManager().registerEvents(new PlayerHeadDropDeathListener(new PlayerHeadDropModifier()), this);
        }
    }

    public static boolean isPaper() {
        try {
            Class.forName("io.papermc.paper.configuration.Configuration");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
