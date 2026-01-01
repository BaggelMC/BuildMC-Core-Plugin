package net.mathias2246.buildmc.commands;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.util.SoundUtil;
import net.mathias2246.buildmc.util.config.YamlConfigurationManager;
import net.mathias2246.buildmc.util.registry.KeyHolder;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static net.mathias2246.buildmc.CoreMain.guides;
import static net.mathias2246.buildmc.CoreMain.plugin;

public class GuidesCommand extends YamlConfigurationManager {


    public static boolean enabled = true;

    /**
     * @param plugin       The plugin that owns this configuration
     * @param resourceName The name of the default configuration resource
     */
    public GuidesCommand(@NotNull Plugin plugin, @NotNull String resourceName) {
        super(CoreMain.plugin, resourceName);
    }

    public static int showGuide(@NotNull CommandSender sender, String key) {
        if (key == null) return 0;

        key = key.toLowerCase();

        NamespacedKey n = NamespacedKey.fromString("buildmc:" + key);

        if (n == null || !guides.contains(n)) {
            if (sender instanceof Player player) plugin.getSoundManager().playSound(player, SoundUtil.mistake);
            plugin.sendMessage(sender, Component.translatable("messages.guides.guide-not-found"));
            return 0;
        }

        if (sender instanceof Player player) plugin.getSoundManager().playSound(player, SoundUtil.notification);
        plugin.sendMessage(sender, Objects.requireNonNull(guides.get(n)).getValue());
        return 1;
    }

    public static CompletableFuture<Suggestions> buildSuggestions(SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase();

        for (var si : CoreMain.guides.keySet()) {
            var statusId = si.toString().replace("buildmc:", "");
            if (statusId.toLowerCase().startsWith(remaining)) {
                builder.suggest(statusId);
            }
        }

        return builder.buildFuture();
    }

    /**
     * Executed when the config file gets loaded.
     * Used to set up all information.
     */
    @Override
    public void setupConfiguration() {

        for (var g : Objects.requireNonNull(configuration.getConfigurationSection("guides")).getValues(false).entrySet()) {
            guides.addEntry(
                    new KeyHolder<>(
                            Objects.requireNonNull(NamespacedKey.fromString("buildmc:" + g.getKey())),
                            MiniMessage.miniMessage().deserialize((String)g.getValue()))
            );
        }

    }

    /**
     * Executed before writing to disk
     */
    @Override
    protected void preSave() {

    }
}
