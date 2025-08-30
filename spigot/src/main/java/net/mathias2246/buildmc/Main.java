package net.mathias2246.buildmc;

import dev.jorel.commandapi.CommandAPI;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.mathias2246.buildmc.api.claims.ClaimManager;
import net.mathias2246.buildmc.api.endevent.EndManager;
import net.mathias2246.buildmc.api.spawnelytra.ElytraManagerImpl;
import net.mathias2246.buildmc.api.spawneyltra.ElytraManager;
import net.mathias2246.buildmc.api.item.CustomItemListener;
import net.mathias2246.buildmc.api.item.CustomItemRegistry;
import net.mathias2246.buildmc.claims.ClaimCommand;
import net.mathias2246.buildmc.claims.ClaimManagerImpl;
import net.mathias2246.buildmc.claims.ClaimToolParticles;
import net.mathias2246.buildmc.claims.tools.ClaimSelectionTool;
import net.mathias2246.buildmc.commands.BuildMcCommand;
import net.mathias2246.buildmc.commands.CommandRegister;
import net.mathias2246.buildmc.endEvent.EndListener;
import net.mathias2246.buildmc.endEvent.EndManagerImpl;
import net.mathias2246.buildmc.platform.SoundManagerSpigotImpl;
import net.mathias2246.buildmc.player.PlayerHeadDropModifier;
import net.mathias2246.buildmc.playerHeads.PlayerHeadDropDeathListener;
import net.mathias2246.buildmc.spawnElytra.DisableRocketListener;
import net.mathias2246.buildmc.spawnElytra.ElytraZoneCommand;
import net.mathias2246.buildmc.spawnElytra.ElytraZoneManager;
import net.mathias2246.buildmc.spawnElytra.SpawnBoostListener;
import net.mathias2246.buildmc.status.PlayerStatus;
import net.mathias2246.buildmc.status.SetStatusCommand;
import net.mathias2246.buildmc.status.StatusConfig;
import net.mathias2246.buildmc.util.Message;
import net.mathias2246.buildmc.util.RegistriesHolder;
import net.mathias2246.buildmc.util.SoundManager;
import net.mathias2246.buildmc.util.config.ConfigurationValidationException;
import net.mathias2246.buildmc.util.language.LanguageManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Logger;

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

    public static CustomItemRegistry customItems;

    public static ClaimManager apiClaimManager;
    public static EndManager apiEndManager;
    private static ElytraManager apiElytraManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        logger = this.getLogger();

        pluginFolder = plugin.getDataFolder();
        if (!pluginFolder.exists()) {
            //noinspection ResultOfMethodCallIgnored
            pluginFolder.mkdir();
        }

        configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) this.saveResource("config.yml", false);
        config = this.getConfig();

        CoreMain.initialize(this);

        LanguageManager.init();

        audiences = BukkitAudiences.create(plugin);

        SoundManagerSpigotImpl.setup();
        CoreMain.soundManager = new SoundManagerSpigotImpl();

        apiClaimManager = new ClaimManagerImpl();
        apiEndManager = new EndManagerImpl();
        apiElytraManager = new ElytraManagerImpl(zoneManager);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        audiences.close();
        CommandAPI.onDisable();

        CoreMain.stop();
    }

    private void registerEvent(@NotNull Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    private void disableCommand(String namespace, String commandName) {
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());

            if (!(commandMap instanceof SimpleCommandMap)) {
                logger.warning("Unsupported CommandMap implementation. Cannot disable command: " + commandName);
                return;
            }

            // Cast to access knownCommands
            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);

            // Remove all relevant keys
            knownCommands.keySet().removeIf(key ->
                    key.equalsIgnoreCase(commandName) ||
                            key.equalsIgnoreCase(namespace + ":" + commandName) ||
                            key.endsWith(":" + commandName));

            // Register dummy override
            Command blockedCommand = new Command(commandName) {
                @Override
                public boolean execute(@NotNull CommandSender sender, @NotNull String label, String[] args) {
                    audiences.sender(sender).sendMessage(Message.msg(sender, "messages.error.command-disabled"));
                    return true;
                }
            };

            commandMap.register(namespace, blockedCommand);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.warning("Failed to fully disable command '" + commandName + "': " + e.getMessage());
        }
    }


    private CommandMap getCommandMap() {
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            return (CommandMap) commandMapField.get(Bukkit.getServer());
        } catch (Exception e) {
            logger.warning("Failed to retrieve the command map.");
            return null;
        }
    }

    @Override
    public void sendPlayerMessage(Player player, Component message) {
        audiences.player(player).sendMessage(message);
    }

    @Override
    public void sendPlayerActionBar(Player player, Component message) {
        // audiences.player(player).sendActionBar(message);

        // This is stupid, but you can't argue with the results
        BaseComponent[] components = BungeeComponentSerializer.get().serialize(message);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, components);
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

    private final RegistriesHolder registriesHolder = new RegistriesHolder.Builder().build();

    @Override
    public @NotNull RegistriesHolder getRegistriesHolder() {
        return registriesHolder;
    }

    @Override
    public void finishLoading() {
        customItems = new CustomItemRegistry();
        getServer().getPluginManager().registerEvents(new CustomItemListener(customItems), this);

        try {
            EndListener.loadFromConfig();
        } catch (ConfigurationValidationException e) {
            throw new RuntimeException(e);
        }

        CommandRegister.setupCommandAPI();
        CommandRegister.register(new BuildMcCommand());

        getServer().getPluginManager().registerEvents(new EndListener(), this);

        if (config.getBoolean("spawn-elytra.enabled")) {
            getServer().getPluginManager().registerEvents(new SpawnBoostListener(zoneManager), this);
            if (config.getBoolean("spawn-elytra.disable-rockets")) getServer().getPluginManager().registerEvents(new DisableRocketListener(), this);
            CommandRegister.register(new ElytraZoneCommand(zoneManager));
            zoneManager.loadZoneFromConfig();
        }

        if (config.getBoolean("claims.enabled")) {
            customItems.register(
                    new ClaimSelectionTool(this,
                            Objects.requireNonNull(NamespacedKey.fromString("buildmc:claim_tool")),
                            new ClaimToolParticles.Builder()
                    )
            );

            CommandRegister.register(new ClaimCommand());
        }

        if (config.getBoolean("status.enabled")) {
            statusConfig = new StatusConfig(this);
            CommandRegister.register(new SetStatusCommand(statusConfig));
            getServer().getPluginManager().registerEvents(new PlayerStatus(), this);
        }

        if (config.getBoolean("disable-commands")) {
            if (config.isList("disabled-commands")) {
                for (String fullCommand : config.getStringList("disabled-commands")) {
                    String[] parts = fullCommand.split(":", 2);
                    if (parts.length == 2) {
                        disableCommand(parts[0], parts[1]);
                    } else {
                        logger.warning("Invalid command format in 'disabled-commands': " + fullCommand);
                    }
                }
            }
        }

        if (config.getBoolean("player-head.on-death")) {
            getServer().getPluginManager().registerEvents(new PlayerHeadDropDeathListener(new PlayerHeadDropModifier()), this);
        }
    }
}
