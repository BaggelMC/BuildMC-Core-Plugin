package net.mathias2246.buildmc.util;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;


public abstract class ConfigurationManager {

    private final Plugin plugin;

    public Plugin getPlugin() {
        return plugin;
    }

    private File configFile;

    private final @NotNull String resourceName;

    public final FileConfiguration configuration;

    public ConfigurationManager(@NotNull Plugin plugin, @NotNull String resourceName) {
        this.plugin = plugin;
        this.resourceName = resourceName;
        this.configFile = new File(plugin.getDataFolder(), resourceName);

        saveFromResource();
        configuration = YamlConfiguration.loadConfiguration(configFile);
        setupConfiguration();
    }

    public File getConfigFile() {
        return configFile;
    }
    public void setConfigFile(@NotNull File file) {
        configFile = file;
        try {
            load();
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public void save() throws IOException {
        configuration.save(configFile);
    }

    public abstract void setupConfiguration();

    private void load() throws IOException, InvalidConfigurationException {
        configuration.load(configFile);
        setupConfiguration();
    }

    public void saveFromResource() {
        if (!configFile.exists()) plugin.saveResource(resourceName, false);

    }
}
