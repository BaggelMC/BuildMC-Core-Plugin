package net.mathias2246.buildmc.util;

import net.mathias2246.buildmc.util.language.LanguageManager;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

public class Message {

    private static final Locale DEFAULT_LOCALE = Locale.forLanguageTag("en-US");
    private static LanguageManager languageManager;

    /**
     * Initializes the Message class with a LanguageManager.
     * Must be called before any message methods are used.
     *
     * @param manager the LanguageManager instance used for translations
     * @throws IllegalStateException if called more than once
     */
    public static void init(LanguageManager manager) {
        if (languageManager != null) {
            throw new IllegalStateException("Message.init() was already called");
        }
        languageManager = manager;
    }

    /**
     * Ensures the Message class has been initialized.
     *
     * @throws IllegalStateException if Message.init() was not called before usage
     */
    private static void ensureInitialized() {
        if (languageManager == null) {
            throw new IllegalStateException("Message.init(LanguageManager) must be called before using messages.");
        }
    }

    /**
     * Retrieves the locale from a CommandSender.
     * If the sender is a Player, the player's locale is used.
     * Otherwise, the default locale (en-US) is returned.
     *
     * @param sender the CommandSender whose locale is retrieved
     * @return the Locale of the sender or the default locale
     */
    private static Locale getLocale(CommandSender sender) {
        if (sender instanceof Player player) {
            return Locale.forLanguageTag(player.getLocale().replace('_', '-'));
        }
        return DEFAULT_LOCALE;
    }

    /**
     * Retrieves the locale from a Player.
     *
     * @param player the Player whose locale is retrieved
     * @return the Locale of the player
     */
    private static Locale getLocale(Player player) {
        return Locale.forLanguageTag(player.getLocale().replace('_', '-'));
    }

    /**
     * Translates a message key into a BaseComponent array for a player,
     * without any placeholders.
     *
     * @param player the player to whom the message will be sent
     * @param key    the message key to translate
     * @return the translated message as a BaseComponent array
     */
    public static BaseComponent[] msg(Player player, String key) {
        return msg(player, key, Collections.emptyMap());
    }

    /**
     * Translates a message key into a BaseComponent array for a player,
     * replacing placeholders with their respective values.
     *
     * @param player       the player to whom the message will be sent
     * @param key          the message key to translate
     * @param placeholders a map of placeholder keys to replacement strings
     * @return the translated message as a BaseComponent array
     */
    public static BaseComponent[] msg(Player player, String key, Map<String, String> placeholders) {
        ensureInitialized();
        return languageManager.translate(getLocale(player), key, placeholders);
    }

    /**
     * Sends a standardized error message to a player, embedding the provided error message.
     *
     * @param player       the player to whom the error message will be sent
     * @param errorMessage the error message to embed
     * @return the translated error message as a BaseComponent array
     */
    public static BaseComponent[] errorMsg(Player player, String errorMessage) {
        return msg(player, "messages.error.general", Map.of("error_message", errorMessage));
    }

    /**
     * Translates a message key into a BaseComponent array for a CommandSender,
     * without placeholders.
     *
     * @param sender the CommandSender to whom the message will be sent
     * @param key    the message key to translate
     * @return the translated message as a BaseComponent array
     */
    public static BaseComponent[] msg(CommandSender sender, String key) {
        return msg(sender, key, Collections.emptyMap());
    }

    /**
     * Translates a message key into a BaseComponent array for a CommandSender,
     * replacing placeholders with their respective values.
     *
     * @param sender       the CommandSender to whom the message will be sent
     * @param key          the message key to translate
     * @param placeholders a map of placeholder keys to replacement strings
     * @return the translated message as a BaseComponent array
     */
    public static BaseComponent[] msg(CommandSender sender, String key, Map<String, String> placeholders) {
        ensureInitialized();
        Locale locale = getLocale(sender);
        return languageManager.translate(locale, key, placeholders);
    }

    /**
     * Sends a standardized error message to a CommandSender, embedding the provided error message.
     *
     * @param sender       the CommandSender to whom the error message will be sent
     * @param errorMessage the error message to embed
     * @return the translated error message as a BaseComponent array
     */
    public static BaseComponent[] errorMsg(CommandSender sender, String errorMessage) {
        return msg(sender, "messages.error.general", Map.of("error_message", errorMessage));
    }

    /**
     * Retrieves the raw untranslated message string for the given key and sender's locale.
     *
     * @param sender the CommandSender whose locale will be used
     * @param key    the message key to retrieve
     * @return the raw message string
     */
    public static String raw(CommandSender sender, String key) {
        ensureInitialized();
        return languageManager.getRawMessage(getLocale(sender), key);
    }
}
