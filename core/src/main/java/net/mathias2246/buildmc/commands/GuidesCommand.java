package net.mathias2246.buildmc.commands;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.Guide;
import net.mathias2246.buildmc.util.AudienceUtil;
import net.mathias2246.buildmc.util.Message;
import net.mathias2246.buildmc.util.SoundUtil;
import net.mathias2246.buildmc.util.ValidationPatterns;
import net.mathias2246.buildmc.util.config.YamlConfigurationManager;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static net.mathias2246.buildmc.CoreMain.guidesRegistry;
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

        if (!(sender instanceof Player player)) {
            // Fallback to chat for non-player senders
            key = key.toLowerCase();
            NamespacedKey n = NamespacedKey.fromString("buildmc:" + key);
            if (n == null || !guidesRegistry.contains(n)) {
                AudienceUtil.sendMessage(sender, Component.translatable("messages.guides.guide-not-found"));
                return 0;
            }
            AudienceUtil.sendMessage(sender, Message.msg(sender, Objects.requireNonNull(guidesRegistry.get(n)).getTextKey()));
            return 1;
        }

        key = key.toLowerCase();
        NamespacedKey n = NamespacedKey.fromString("buildmc:" + key);

        if (n == null || !guidesRegistry.contains(n)) {
            plugin.getSoundManager().playSound(player, SoundUtil.mistake);
            AudienceUtil.sendMessage(sender, Component.translatable("messages.guides.guide-not-found"));
            return 0;
        }

        Component guideContent = Message.msg(player, Objects.requireNonNull(guidesRegistry.get(n)).getTextKey());

        String finalKey = key;
        Collection<Map.Entry<String, URL>> urlButtons = Objects.requireNonNull(guidesRegistry.get(n)).getUrls().entrySet();

        // Build the action buttons
        List<ActionButton> actions = new ArrayList<>();

        // Add general exit button, so that no errors are thrown when there are no url buttons
        actions.add(
                ActionButton.builder(Message.msg(player, "messages.claims.ui.general.exit"))
                        .action(
                                null
                        )
                        .build()
        );

        for (Map.Entry<String, URL> entry : urlButtons) {
            actions.add(
                    ActionButton.builder(
                            Message.msg(player, entry.getKey())
                    )
                            .action(
                                    DialogAction.staticAction(
                                            ClickEvent.openUrl(entry.getValue())
                                    )
                            )
                            .build()
            );
        }

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Message.msg(player, "messages.guides.dialog-title", Map.of("guide", finalKey)))
                        .body(List.of(
                                DialogBody.plainMessage(guideContent, 512)
                        ))
                        .build()
                )
                .type(
                        DialogType.multiAction(
                                actions
                        ).build()
                )

        );


        plugin.getSoundManager().playSound(player, SoundUtil.notification);
        player.showDialog(dialog);
        return 1;
    }

    public static CompletableFuture<Suggestions> buildSuggestions(SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase();

        for (var si : CoreMain.guidesRegistry.keySet()) {
            var statusId = si.value();
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

        var guidesSection = configuration.getConfigurationSection("guides");
        if (guidesSection == null) {
            return;
        }

        Map<String, Guide> guides = new LinkedHashMap<>();

        Set<String> guideKeys = guidesSection.getKeys(false); // top-level keys only
        for (String guideId : guideKeys) {
            ConfigurationSection section = guidesSection.getConfigurationSection(guideId);
            if (section == null) continue;

            String textKey = section.getString("text-key");
            if (textKey == null) {
                getPlugin().getLogger().warning("Could not find key 'text-key' for guide '" + guideId + "' (Skipped guide)");
                continue;
            }

            // Skip if YAML path to the translation key is malformed
            if (!ValidationPatterns.YAML_PATH.matcher(textKey).matches()) {
                getPlugin().getLogger().warning("Invalid text-key in guide '" + guideId + "' (Skipped guide)");
                return;
            }

            Map<String, URL> urls = new LinkedHashMap<>();
            ConfigurationSection urlsSection = section.getConfigurationSection("urls");
            if (urlsSection != null) {
                urlsSection.getKeys(false).stream()
                        .filter(k -> k.matches("-?\\d+")) // Allow positive and negative integers as keys
                        .map(Integer::parseInt)
                        .sorted()
                        .forEach(index -> {
                            ConfigurationSection entry = urlsSection.getConfigurationSection(String.valueOf(index));
                            if (entry == null) return;
                            String labelKey = entry.getString("label-key");
                            String urlStr   = entry.getString("url");
                            // Skip the URL is missing
                            if (urlStr == null) {
                                getPlugin().getLogger().warning("Missing key 'url' for url button in guide '" + guideId + "' with order number " + index + " (Skipped URL)");
                                return;
                            }
                            // Skip if the label for the button is missing
                            if (labelKey == null) {
                                getPlugin().getLogger().warning("Missing key 'label-key' for url button in guide '" + guideId + "' with order number " + index + " (Skipped URL)");
                                return;
                            }

                            URL url;
                            try {
                                url = new URI(urlStr).toURL();
                            } catch (Exception ignore) {
                                getPlugin().getLogger().warning("Invalid URL '" + urlStr + "' for url button in guide '" + guideId + "' with order number " + index + " (Skipped URL)");
                                return;
                            }

                            urls.put(labelKey, url);
                        });
            }

            @Subst("guide-id") String lowerCaseId = guideId.toLowerCase();
            guides.put(guideId, new Guide(Key.key("buildmc", lowerCaseId), textKey, urls));
        }

        guidesRegistry.addEntries(
                plugin,
                guides.values()
        );

    }

    /**
     * Executed before writing to disk
     */
    @Override
    protected void preSave() {

    }
}
