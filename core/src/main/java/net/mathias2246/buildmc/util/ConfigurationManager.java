package net.mathias2246.buildmc.util;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**An abstract utility-class for managing a configuration*/
public abstract class ConfigurationManager {

    private final Plugin plugin;

    /**Gets the plugin that owns this configuration*/
    public Plugin getPlugin() {
        return plugin;
    }

    private File configFile;

    private final @NotNull String resourceName;

    /**The configuration instance that is managed by this ConfigurationManager*/
    public final FileConfiguration configuration;

    /**@param plugin The plugin that owns this configuration
     * @param resourceName The name of the default configuration resource*/
    public ConfigurationManager(@NotNull Plugin plugin, @NotNull String resourceName) {
        this.plugin = plugin;
        this.resourceName = resourceName;
        this.configFile = new File(plugin.getDataFolder(), resourceName);

        saveFromResource();
        configuration = YamlConfiguration.loadConfiguration(configFile);
        setupConfiguration();
    }

    /**The configuration file*/
    public File getConfigFile() {
        return configFile;
    }

    /**Sets the file that this ConfigurationManager accesses.
     * This also executes the setupConfiguration method to set up all the data after loading from the disk*/
    public void setConfigFile(@NotNull File file) {
        configFile = file;
        try {
            load();
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**Saves the configuration to the current config file.
     * Before saving, execute the preSave method with your logic of processing and formating the data that is saved.*/
    public void save() throws IOException {
        preSave();
        configuration.save(configFile);
    }

    /**Executed when the config file gets loaded.
     * Used to set up all information.*/
    public abstract void setupConfiguration();

    private void load() throws IOException, InvalidConfigurationException {
        configuration.load(configFile);
        setupConfiguration();
    }

    /**Executed before writing to disk*/
    protected abstract void preSave();

    /**Saves the default config from the plugins resources if the file doesn't exist*/
    public void saveFromResource() {
        if (!configFile.exists()) plugin.saveResource(resourceName, false);

    }
}
