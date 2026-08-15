package net.mathias2246.buildmc.util;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

public final class AudienceUtil {

    public static void sendMessage(@NotNull Audience audience, Component message) {
        audience.sendMessage(message);
    }

    public static void sendActionBar(@NotNull Audience audience, Component message) {
        audience.sendActionBar(message);
    }

    /** Resolves placeholders inside a MiniMessage string and displays it to a {@link CommandSender}.
     *
     * @param sender The {@link CommandSender} to send the rich message to.
     * @param miniMessage The MiniMessage string to parse.<br>
     *                    Resolves custom tags e.g. "Hello &lt;player-name&gt;!" using the entries inside the richPlaceholders map
     * @param richPlaceholders A {@link Map} of placeholders to resolve.
     *                         The key is the name inside the custom mini message tag.<br>
     *                         The entry can be a {@link Component} or {@link TagResolver.Single}.<br>
     *                         Every other type will be converted using {@link Object#toString()} and is parsed as mini message text.
     * **/
    public static void sendRichMessage(@NotNull CommandSender sender, @NotNull String miniMessage, @NotNull Map<String, ?> richPlaceholders) {
        TagResolver.Single[] resolvers = mapPlaceholders(richPlaceholders);

        sender.sendRichMessage(
                miniMessage,
                resolvers
        );
    }

    /** Resolves placeholders inside a MiniMessage string and renders translations using the {@link GlobalTranslator#render(Component, Locale)} method.
     * <p>
     * This can be used for creating rendered component names for {@link org.bukkit.inventory.ItemStack}s because {@link net.kyori.adventure.text.TranslatableComponent}s
     * cannot be translated on the server-side automatically.
     * </p>
     *
     * @param sender The {@link CommandSender} to send the rich message to.
     * @param miniMessage The MiniMessage string to parse.<br>
     *                    Resolves custom tags e.g. "Hello &lt;player-name&gt;!" using the entries inside the richPlaceholders map
     * @param richPlaceholders A {@link Map} of placeholders to resolve.
     *                         The key is the name inside the custom mini message tag.<br>
     *                         The entry can be a {@link Component} or {@link TagResolver.Single}.<br>
     *                         Every other type will be converted using {@link Object#toString()} and is parsed as mini message text.
     * **/
    public static Component renderRichMessage(@NotNull CommandSender sender, @NotNull String miniMessage, @NotNull Map<String, ?> richPlaceholders) {
        TagResolver.Single[] resolvers = mapPlaceholders(richPlaceholders);

        Component raw = MiniMessage.miniMessage().deserialize(
                miniMessage,
                resolvers
        );

        Locale locale = Locale.US;
        if (sender instanceof Player player) locale = player.locale();

        return GlobalTranslator.render(raw, locale);
    }

    public static @NotNull String renderPlainMessage(@NotNull CommandSender sender, @NotNull String miniMessage, @NotNull Map<String, ?> plainPlaceholders) {
        return PlainTextComponentSerializer.plainText().serialize(renderRichMessage(sender, miniMessage, plainPlaceholders));
    }

    public static TagResolver.Single[] mapPlaceholders(Map<String, ?> richPlaceholders) {
        return richPlaceholders.entrySet().stream().map(
                entry -> {
                    @Subst("key") String key = entry.getKey();
                    if (entry.getValue() instanceof Component component) {
                        return Placeholder.component(key, component);
                    }
                    if (entry.getValue() instanceof TagResolver.Single placeholder) {
                        return placeholder;
                    }
                    if (entry.getValue() instanceof Supplier<?> supplier) {
                        return Placeholder.parsed(key, supplier.get().toString());
                    }
                    else {
                        return Placeholder.parsed(key, entry.getValue().toString());
                    }
                }
        ).toArray(TagResolver.Single[]::new);
    }
}
