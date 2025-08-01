package net.mathias2246.buildmc;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.mathias2246.buildmc.claims.ClaimCommand;
import net.mathias2246.buildmc.claims.ClaimListener;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.claims.ClaimTool;
import net.mathias2246.buildmc.commands.BuildMcCommand;
import net.mathias2246.buildmc.endEvent.EndListener;
import net.mathias2246.buildmc.spawnElytra.DisableBoostListener;
import net.mathias2246.buildmc.spawnElytra.ElytraZoneManager;
import net.mathias2246.buildmc.spawnElytra.SpawnBoostListener;
import net.mathias2246.buildmc.status.PlayerStatus;
import net.mathias2246.buildmc.status.SetStatusCommand;
import net.mathias2246.buildmc.status.StatusConfig;
import net.mathias2246.buildmc.util.Message;
import net.mathias2246.buildmc.util.Sounds;
import net.mathias2246.buildmc.util.language.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.logging.Logger;

public final class Main extends JavaPlugin {

    public static Logger logger;

    public static Plugin plugin;

    @Subst("")
    public static FileConfiguration config;
    public static File configFile;

    public static File pluginFolder;

    public static final ElytraZoneManager zoneManager = new ElytraZoneManager();

    public static StatusConfig statusConfig;

    public static ClaimManager claimManager;

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

        LanguageManager.init();

        configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) this.saveResource("config.yml", false);
        config = this.getConfig();

        EndListener.loadFromConfig();

        Sounds.setup();
        ClaimTool.setup();

        getServer().getPluginManager().registerEvents(new EndListener(), this);

        getServer().getPluginManager().registerEvents(new ClaimTool(), this);

        if (config.getBoolean("spawn-elytra.enabled")) {
            getServer().getPluginManager().registerEvents(new SpawnBoostListener(zoneManager), this);
            if (config.getBoolean("spawn-elytra.disable-rockets")) getServer().getPluginManager().registerEvents(new DisableBoostListener(), this);

            zoneManager.loadZoneFromConfig();
        }

        if (config.getBoolean("claims.enabled")) {
            getServer().getPluginManager().registerEvents(new ClaimListener(), this);
            claimManager = new ClaimManager(this, "claim-whitelists.yml");
        }

        if (config.getBoolean("status.enabled")) {
            statusConfig = new StatusConfig();
            getServer().getPluginManager().registerEvents(new PlayerStatus(), this);
        }

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(new BuildMcCommand().getCommand());

            if (config.getBoolean("status.enabled")) commands.registrar().register(new SetStatusCommand(statusConfig).getCommand());

            if (config.getBoolean("claims.enabled")) commands.registrar().register(new ClaimCommand().getCommand());
        });


        if (config.getBoolean("disable-reload-command")) {
            disableCommand("bukkit", "reload");
            disableCommand("bukkit", "rl");
            disableCommand("bukkit", "bukkitreload");
        }
        if (config.getBoolean("disable-seed-command")) {
            disableCommand("minecraft", "seed");
        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

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
}
