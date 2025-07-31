package net.mathias2246.buildmc.util;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import static net.mathias2246.buildmc.Main.plugin;
import static net.mathias2246.buildmc.Main.pluginFolder;

public abstract class ConfigurationManager {

    private File configFile;

    private final @NotNull String resourceName;

    public final FileConfiguration configuration;

    public ConfigurationManager(@NotNull String resourceName) {
        this.resourceName = resourceName;
        this.configFile = new File(pluginFolder, resourceName);

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
