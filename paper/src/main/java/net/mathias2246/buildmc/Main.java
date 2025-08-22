package net.mathias2246.buildmc;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.claims.*;
import net.mathias2246.buildmc.claims.listeners.*;
import net.mathias2246.buildmc.claims.tools.ClaimSelectionTool;
import net.mathias2246.buildmc.commands.BuildMcCommand;
import net.mathias2246.buildmc.endEvent.EndListener;
import net.mathias2246.buildmc.item.CustomItemListener;
import net.mathias2246.buildmc.item.CustomItemRegistry;
import net.mathias2246.buildmc.platform.SoundManagerPaperImpl;
import net.mathias2246.buildmc.player.PlayerHeadDropModifier;
import net.mathias2246.buildmc.playerHeads.PlayerHeadDropDeathListener;
import net.mathias2246.buildmc.spawnElytra.DisableBoostListener;
import net.mathias2246.buildmc.spawnElytra.ElytraZoneManager;
import net.mathias2246.buildmc.spawnElytra.SpawnBoostListener;
import net.mathias2246.buildmc.status.PlayerStatus;
import net.mathias2246.buildmc.status.SetStatusCommand;
import net.mathias2246.buildmc.status.StatusConfig;
import net.mathias2246.buildmc.util.Message;
import net.mathias2246.buildmc.util.config.ConfigurationValidationException;
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
import org.bukkit.plugin.java.JavaPlugin;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

@SuppressWarnings("UnstableApiUsage")
public final class Main extends JavaPlugin implements MainClass {

    public static Logger logger;

    public static Plugin plugin;

    @Subst("")
    public static FileConfiguration config;
    public static File configFile;

    public static File pluginFolder;

    public static final ElytraZoneManager zoneManager = new ElytraZoneManager();

    public static StatusConfig statusConfig;

    public static CustomItemRegistry customItems;

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

        try {
            EndListener.loadFromConfig();
        } catch (ConfigurationValidationException e) {
            throw new RuntimeException(e);
        }

        SoundManagerPaperImpl.setup();
        CoreMain.soundManager = new SoundManagerPaperImpl();
        customItems = new CustomItemRegistry();
        getServer().getPluginManager().registerEvents(new CustomItemListener(customItems), this);

        getServer().getPluginManager().registerEvents(new EndListener(), this);
        if (config.getBoolean("spawn-elytra.enabled")) {
            getServer().getPluginManager().registerEvents(new SpawnBoostListener(zoneManager), this);
            if (config.getBoolean("spawn-elytra.disable-rockets")) getServer().getPluginManager().registerEvents(new DisableBoostListener(), this);

            zoneManager.loadZoneFromConfig();
        }

        if (config.getBoolean("claims.enabled")) {

            customItems.register(
                    new ClaimSelectionTool(this,
                            Objects.requireNonNull(NamespacedKey.fromString("buildmc:claim_tool")),
                            new ClaimToolParticles.Builder()
                    )
            );

            registerEvent(new ClaimContainerListener());
            registerEvent(new ClaimExplosionsListener());
            registerEvent(new ClaimBreakListener());
            registerEvent(new ClaimPlaceListener());
            registerEvent(new ClaimDamageProtectionListener());
            registerEvent(new ClaimSignEditListener());
            registerEvent(new ClaimInteractionListener());
            registerEvent(new ClaimPotionSplashListener());
            registerEvent(new ClaimVehicleEnterListener());
            registerEvent(new ClaimBucketUseEvent());
            registerEvent(new ClaimEntityChangeBlockListener());
            registerEvent(new ClaimItemPickupListener());
            registerEvent(new ClaimItemDropListener());
            registerEvent(new ClaimFrostWalkerListener());
            registerEvent(new ClaimPistonMovementListener());
            registerEvent(new ClaimBeehiveInteractListener());
            registerEvent(new ClaimBonemealInteractListener());
            registerEvent(new ClaimEntityLeashListener());
            registerEvent(new ClaimItemFrameRotateListener());
            registerEvent(new ClaimNameTagUseListener());
            registerEvent(new ClaimPaintingInteractListener());
        }

        if (config.getBoolean("status.enabled")) {
            statusConfig = new StatusConfig(this);
            getServer().getPluginManager().registerEvents(new PlayerStatus(), this);
        }

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(new BuildMcCommand().getCommand());

            if (config.getBoolean("status.enabled")) commands.registrar().register(new SetStatusCommand(statusConfig).getCommand());

            if (config.getBoolean("claims.enabled")) commands.registrar().register(new ClaimCommand().getCommand());
        });


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

    @Override
    public void onDisable() {
        // Plugin shutdown logic

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
                public boolean execute(@NotNull CommandSender sender, @NotNull String label, String @NotNull [] args) {
                    sender.sendMessage(Message.msg(sender, "messages.error.command-disabled"));
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
        player.sendMessage(message);
    }

    @Override
    public void sendPlayerActionBar(Player player, Component message) {
        player.sendActionBar(message);
    }
}
