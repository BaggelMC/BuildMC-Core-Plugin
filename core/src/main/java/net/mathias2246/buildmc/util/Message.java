package net.mathias2246.buildmc.util;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.util.language.LanguageManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * Utility class for retrieving localized messages for players and command senders.
 * Handles translation of messages with support for placeholders and locale-specific translations.
 */
public class Message {

    private static final Locale DEFAULT_LOCALE = Locale.forLanguageTag("en-US");

    /**
     * Retrieves the locale of the specified CommandSender, defaulting to en-US if not a Player.
     *
     * @param sender the command sender
     * @return the locale of the sender
     */
    private static Locale getLocale(CommandSender sender) {
        if (sender instanceof Player player) {
            return Locale.forLanguageTag(player.getLocale().replace('_', '-'));
        }
        return DEFAULT_LOCALE;
    }

    /**
     * Retrieves the locale of the specified Player.
     *
     * @param player the player
     * @return the locale of the player
     */
    private static Locale getLocale(Player player) {
        return Locale.forLanguageTag(player.getLocale().replace('_', '-'));
    }

    // === Component methods ===

    /**
     * Retrieves a localized message as a Component for the specified player.
     *
     * @param player the player
     * @param key the translation key
     * @return the localized Component
     */
    public static Component msg(Player player, String key) {
        return msg(player, key, Collections.emptyMap());
    }

    /**
     * Retrieves a localized message as a Component with placeholders for the specified player.
     *
     * @param player the player
     * @param key the translation key
     * @param placeholders map of placeholders to be replaced in the message
     * @return the localized Component
     */
    public static Component msg(Player player, String key, Map<String, String> placeholders) {
        return LanguageManager.translate(getLocale(player), key, placeholders);
    }

    /**
     * Constructs a general error message for the specified player.
     *
     * @param player the player
     * @param errorMessage the error message to include
     * @return the error message as a Component
     */
    public static Component errorMsg(Player player, String errorMessage) {
        return msg(player, "messages.error.general", Map.of("error_message", errorMessage));
    }

    /**
     * Constructs a permission error message for the specified player.
     *
     * @param player the player
     * @return the permission error message as a Component
     */
    public static Component permissionErrorMsg(Player player) {
        return msg(player, "messages.error.no-permission");
    }

    /**
     * Constructs an error message for when the command is not executed by a player.
     *
     * @param player the player
     * @return the not-a-player error message as a Component
     */
    public static Component noPlayerErrorMsg(Player player) {
        return msg(player, "messages.error.not-a-player");
    }

    /**
     * Retrieves a localized message as a Component for the specified CommandSender.
     *
     * @param sender the sender
     * @param key the translation key
     * @return the localized Component
     */
    public static Component msg(CommandSender sender, String key) {
        return msg(sender, key, Collections.emptyMap());
    }

    /**
     * Retrieves a localized message as a Component with placeholders for the specified CommandSender.
     *
     * @param sender the sender
     * @param key the translation key
     * @param placeholders map of placeholders to be replaced in the message
     * @return the localized Component
     */
    public static Component msg(CommandSender sender, String key, Map<String, String> placeholders) {
        return LanguageManager.translate(getLocale(sender), key, placeholders);
    }

    /**
     * Constructs a general error message for the specified CommandSender.
     *
     * @param sender the sender
     * @param errorMessage the error message to include
     * @return the error message as a Component
     */
    public static Component errorMsg(CommandSender sender, String errorMessage) {
        return msg(sender, "messages.error.general", Map.of("error_message", errorMessage));
    }

    /**
     * Constructs a permission error message for the specified CommandSender.
     *
     * @param sender the sender
     * @return the permission error message as a Component
     */
    public static Component permissionErrorMsg(CommandSender sender) {
        return msg(sender, "messages.error.no-permission");
    }

    /**
     * Constructs an error message for when the CommandSender is not a player.
     *
     * @param sender the sender
     * @return the not-a-player error message as a Component
     */
    public static Component noPlayerErrorMsg(CommandSender sender) {
        return msg(sender, "messages.error.not-a-player");
    }

    // === String methods ===

    /**
     * Retrieves a localized message as a String for the specified player.
     *
     * @param player the player
     * @param key the translation key
     * @return the localized message as a String
     */
    public static String msgStr(Player player, String key) {
        return msgStr(player, key, Collections.emptyMap());
    }

    /**
     * Retrieves a localized message as a String with placeholders for the specified player.
     *
     * @param player the player
     * @param key the translation key
     * @param placeholders map of placeholders to be replaced in the message
     * @return the localized message as a String
     */
    public static String msgStr(Player player, String key, Map<String, String> placeholders) {
        return LanguageManager.translateStr(getLocale(player), key, placeholders);
    }

    /**
     * Constructs a general error message as a String for the specified player.
     *
     * @param player the player
     * @param errorMessage the error message to include
     * @return the error message as a String
     */
    public static String errorMsgStr(Player player, String errorMessage) {
        return msgStr(player, "messages.error.general", Map.of("error_message", errorMessage));
    }

    /**
     * Constructs a permission error message as a String for the specified player.
     *
     * @param player the player
     * @return the permission error message as a String
     */
    public static String permissionErrorMsgStr(Player player) {
        return msgStr(player, "messages.error.no-permission");
    }

    /**
     * Constructs an error message for when the command is not executed by a player.
     *
     * @param player the player
     * @return the not-a-player error message as a String
     */
    public static String noPlayerErrorMsgStr(Player player) {
        return msgStr(player, "messages.error.not-a-player");
    }

    /**
     * Retrieves a localized message as a String for the specified CommandSender.
     *
     * @param sender the sender
     * @param key the translation key
     * @return the localized message as a String
     */
    public static String msgStr(CommandSender sender, String key) {
        return msgStr(sender, key, Collections.emptyMap());
    }

    /**
     * Retrieves a localized message as a String with placeholders for the specified CommandSender.
     *
     * @param sender the sender
     * @param key the translation key
     * @param placeholders map of placeholders to be replaced in the message
     * @return the localized message as a String
     */
    public static String msgStr(CommandSender sender, String key, Map<String, String> placeholders) {
        return LanguageManager.translateStr(getLocale(sender), key, placeholders);
    }

    /**
     * Constructs a general error message as a String for the specified CommandSender.
     *
     * @param sender the sender
     * @param errorMessage the error message to include
     * @return the error message as a String
     */
    public static String errorMsgStr(CommandSender sender, String errorMessage) {
        return msgStr(sender, "messages.error.general-str", Map.of("error_message", errorMessage));
    }

    /**
     * Constructs a permission error message as a String for the specified CommandSender.
     *
     * @param sender the sender
     * @return the permission error message as a String
     */
    public static String permissionErrorMsgStr(CommandSender sender) {
        return msgStr(sender, "messages.error.no-permission-str");
    }

    /**
     * Constructs an error message for when the CommandSender is not a player.
     *
     * @param sender the sender
     * @return the not-a-player error message as a String
     */
    public static String noPlayerErrorMsgStr(CommandSender sender) {
        return msgStr(sender, "messages.error.not-a-player-str");
    }
}
