package net.mathias2246.buildmc.util;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.util.language.LanguageManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

public class Message {

    private static final Locale DEFAULT_LOCALE = Locale.forLanguageTag("en-US");

    private static Locale getLocale(CommandSender sender) {
        if (sender instanceof Player player) {
            return Locale.forLanguageTag(player.getLocale().replace('_', '-'));
        }
        return DEFAULT_LOCALE;
    }

    private static Locale getLocale(Player player) {
        return Locale.forLanguageTag(player.getLocale().replace('_', '-'));
    }

    public static Component msg(Player player, String key) {
        return msg(player, key, Collections.emptyMap());
    }

    public static Component msg(Player player, String key, Map<String, String> placeholders) {
        return LanguageManager.translate(getLocale(player), key, placeholders);
    }

    public static Component errorMsg(Player player, String errorMessage) {
        return msg(player, "messages.error.general", Map.of("error_message", errorMessage));
    }

    public static Component msg(CommandSender sender, String key) {
        return msg(sender, key, Collections.emptyMap());
    }

    public static Component msg(CommandSender sender, String key, Map<String, String> placeholders) {
        return LanguageManager.translate(getLocale(sender), key, placeholders);
    }

    public static Component errorMsg(CommandSender sender, String errorMessage) {
        return msg(sender, "messages.error.general", Map.of("error_message", errorMessage));
    }

    public static Component permissionErrorMsg(CommandSender sender) {
        return msg(sender, "messages.error.no-permission");
    }

    public static Component noPlayerErrorMsg(CommandSender sender) {
        return msg(sender, "messages.error.not-a-player");
    }
}
