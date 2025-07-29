package net.mathias2246.buildmc;


import dev.jorel.commandapi.CommandAPI;
import net.mathias2246.buildmc.commands.BuildMcCommand;
import net.mathias2246.buildmc.commands.CommandRegister;
import net.mathias2246.buildmc.commands.ElytraZoneCommand;
import net.mathias2246.buildmc.spawnElytra.ElytraZoneManager;
import net.mathias2246.buildmc.spawnElytra.SpawnBoostListener;
import net.mathias2246.buildmc.status.SetStatusCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.logging.Logger;

public final class Main extends JavaPlugin {

    public static Logger logger;

    public static Plugin plugin;

    public static FileConfiguration config;
    public static File configFile;

    public static File pluginFolder;

    private static final ElytraZoneManager zoneManager = new ElytraZoneManager();


    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        logger = this.getLogger();

        pluginFolder = plugin.getDataFolder();
        if (!pluginFolder.exists()) {
            boolean mkdir = pluginFolder.mkdir();
        }

        configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) this.saveResource("config.yml", false);
        config = this.getConfig();

        CommandRegister.setupCommandAPI();
        CommandRegister.register(new BuildMcCommand());
        CommandRegister.register(new SetStatusCommand());

        if (config.getBoolean("SpawnElytra.Enabled")) {
            getServer().getPluginManager().registerEvents(new SpawnBoostListener(this, zoneManager), this);
            CommandRegister.register(new ElytraZoneCommand(zoneManager));
            zoneManager.loadZoneFromConfig();
        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        CommandAPI.onDisable();
    }
}
