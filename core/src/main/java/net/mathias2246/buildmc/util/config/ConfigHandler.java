package net.mathias2246.buildmc.util.config;

import java.io.IOException;

public interface ConfigHandler {

    /**
     * Generates the config file if it doesn't exist, and populates it with default values.
     */
    void generateConfig();

    /**
     * Loads the configuration file into memory.
     * @throws IOException if the file can't be loaded or read properly.
     */
    void loadConfig() throws IOException;

    /**
     * Reads a value from the configuration.
     * @param key The key to retrieve the value for.
     * @param <T> The type of the value being retrieved.
     * @return The value associated with the key.
     */
    <T> T get(String key);

    /**
     * Reads a value from the configuration with a default value if key is missing.
     * @param key The key to retrieve the value for.
     * @param defaultValue The value to return if the key is missing.
     * @param <T> The type of the value.
     * @return The value associated with the key, or the default value if missing.
     */
    <T> T get(String key, T defaultValue);

    /**
     * Writes a value to the configuration.
     * @param key The key to store the value under.
     * @param value The value to store.
     */
    void set(String key, Object value);

    /**
     * Reloads the configuration, typically after changes have been made externally.
     */
    void reloadConfig();

    /**
     * Validates the configuration to ensure that all necessary keys are present and values are of the expected type.
     * This will throw a ConfigurationValidationException if any value is invalid.
     * @throws ConfigurationValidationException If any configuration value is invalid.
     */
    void validateConfig() throws ConfigurationValidationException;
}
