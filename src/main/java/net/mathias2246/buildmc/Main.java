package net.mathias2246.buildmc;


import dev.jorel.commandapi.CommandAPI;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.mathias2246.buildmc.claims.ClaimTool;
import net.mathias2246.buildmc.commands.BuildMcCommand;
import net.mathias2246.buildmc.commands.CommandRegister;
import net.mathias2246.buildmc.commands.ElytraZoneCommand;
import net.mathias2246.buildmc.endEvent.EndListener;
import net.mathias2246.buildmc.spawnElytra.ElytraZoneManager;
import net.mathias2246.buildmc.spawnElytra.SpawnBoostListener;
import net.mathias2246.buildmc.status.SetStatusCommand;
import net.mathias2246.buildmc.util.Sounds;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.intellij.lang.annotations.Subst;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public final class Main extends JavaPlugin {

    public static Logger logger;

    public static Plugin plugin;

    @Subst("")
    public static FileConfiguration config;
    public static File configFile;

    public static File pluginFolder;

    private static final ElytraZoneManager zoneManager = new ElytraZoneManager();

    public static BukkitAudiences audiences;

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

        audiences = BukkitAudiences.create(plugin);

        EndListener.loadFromConfig();

        Sounds.setup();
        ClaimTool.setup();

        CommandRegister.setupCommandAPI();
        CommandRegister.register(new BuildMcCommand());
        CommandRegister.register(new SetStatusCommand());

        getServer().getPluginManager().registerEvents(new EndListener(), this);

        getServer().getPluginManager().registerEvents(new ClaimTool(), this);

        if (config.getBoolean("spawn-elytra.enabled")) {
            getServer().getPluginManager().registerEvents(new SpawnBoostListener(this, zoneManager), this);
            CommandRegister.register(new ElytraZoneCommand(zoneManager));
            zoneManager.loadZoneFromConfig();
        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        audiences.close();
        CommandAPI.onDisable();
    }
}
