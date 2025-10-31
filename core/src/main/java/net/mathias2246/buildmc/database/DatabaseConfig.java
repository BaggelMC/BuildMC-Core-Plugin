package net.mathias2246.buildmc.database;

import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.util.config.ConfigHandler;
import net.mathias2246.buildmc.util.config.ConfigurationValidationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

public class DatabaseConfig implements ConfigHandler {
    private final File configFile;
    private FileConfiguration config;
    private final Logger logger;

    private static final String USE_SERVER_MODE = "database.useServerMode";
    private static final String SERVER_URL = "database.serverUrl";
    private static final String SERVER_PORT = "database.serverPort";

    public DatabaseConfig() {
        this.configFile = new File(CoreMain.plugin.getDataFolder(), "database.yml");
        this.logger = CoreMain.plugin.getLogger();
    }

    @Override
    public void generateConfig() {
        if (!configFile.exists()) {
            try {
                InputStream defaultConfigStream = getClass().getClassLoader().getResourceAsStream("database.yml");
                if (defaultConfigStream == null) {
                    throw new IOException("Default config file not found in resources.");
                }
                if (!configFile.getParentFile().exists() && !configFile.getParentFile().mkdirs()) {
                    logger.warning("Failed to create directories for config file.");
                }
                Files.copy(defaultConfigStream, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                logger.severe("Error generating config: " + e);
            }
        }
    }

    @Override
    public void loadConfig() throws IOException {
        if (!configFile.exists()) {
            generateConfig();
            if (!configFile.exists()) {
                throw new IOException("Failed to generate and locate database.yml");
            }
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String key) {
        return (T) config.get(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String key, T defaultValue) {
        return (T) config.get(key, defaultValue);
    }

    @Override
    public void set(String key, Object value) {
        config.set(key, value);
        saveConfig();
    }

    @Override
    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    @Override
    public void validateConfig() throws ConfigurationValidationException {
        if (!config.contains(USE_SERVER_MODE)) {
            throw new ConfigurationValidationException("Missing configuration key: " + USE_SERVER_MODE);
        }
        if (!config.contains(SERVER_URL)) {
            throw new ConfigurationValidationException("Missing configuration key: " + SERVER_URL);
        }

        if (!(config.get(USE_SERVER_MODE) instanceof Boolean)) {
            throw new ConfigurationValidationException("Invalid value type for key: " + USE_SERVER_MODE + ". Expected Boolean.");
        }
        if (!(config.get(SERVER_URL) instanceof String)) {
            throw new ConfigurationValidationException("Invalid value type for key: " + SERVER_URL + ". Expected String.");
        }

        if (!(config.get(SERVER_PORT) instanceof  Integer)) {
            throw new ConfigurationValidationException("Invalid value type for key: " + SERVER_PORT + ". Expected Integer.");
        }

        String serverUrl = config.getString(SERVER_URL);
        if (serverUrl == null || serverUrl.isEmpty()) {
            throw new ConfigurationValidationException("Invalid value for key: " + SERVER_URL + ". URL cannot be null or empty.");
        }
    }

    private void saveConfig() {
        if (CoreMain.plugin.getServer().isPrimaryThread()) {
            try {
                config.save(configFile);
            } catch (IOException e) {
                logger.severe("Could not save config database.yml: " + e);
            }
        } else {
            CoreMain.plugin.getServer().getScheduler().runTask(CoreMain.plugin, this::saveConfig);
        }

    }

    public boolean isServerMode() {
        return get(USE_SERVER_MODE, false);
    }

    public String getServerUrl() {
        return get(SERVER_URL, "jdbc:h2:tcp://localhost/./database");
    }

    public int getServerPort() {
        return config.getInt(SERVER_PORT, 9092);
    }
}
