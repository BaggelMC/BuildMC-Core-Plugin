package net.mathias2246.buildmc.util.config;

/**
 * Exception thrown when a configuration fails validation.
 */
public class ConfigurationValidationException extends Exception {

    public ConfigurationValidationException(String message) {
        super(message);
    }

    public ConfigurationValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
