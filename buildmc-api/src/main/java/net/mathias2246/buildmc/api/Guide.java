package net.mathias2246.buildmc.api;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public class Guide implements Keyed, org.bukkit.Keyed {
    private final @NotNull Key key;
    private final @NotNull String textKey;
    private final @NotNull Map<String, URL> urls;

    public Guide(
            @NotNull Key key,
            @NotNull String textKey,
            @NotNull Map<String, URL> urls
    ) {
        this.key = key;
        this.textKey = textKey;
        this.urls = urls;
    }

    public @NotNull String getTextKey() {
        return textKey;
    }

    public @NotNull Map<String, URL> getUrls() {
        return urls;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return Objects.requireNonNull(NamespacedKey.fromString(key.asString()));
    }

    @Override
    public @NonNull Key key() {
        return key;
    }
}
