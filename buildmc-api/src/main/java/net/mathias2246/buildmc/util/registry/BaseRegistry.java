package net.mathias2246.buildmc.util.registry;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;


public class BaseRegistry<T extends Keyed> implements Registry<T> {

    private final HashMap<NamespacedKey, T> registry;

    public BaseRegistry() {
        this.registry = new HashMap<>();
    }

    public @Nullable T set(@NotNull NamespacedKey namespacedKey, @NotNull T value) {
        return registry.put(namespacedKey, value);
    }

    public boolean contains(@NotNull NamespacedKey namespacedKey) {
        return registry.containsKey(namespacedKey);
    }

    public Set<NamespacedKey> keySet() {
        return registry.keySet();
    }

    @Override
    public @Nullable T get(@NotNull NamespacedKey namespacedKey) {
        return registry.get(namespacedKey);
    }

    @Override
    public @NotNull T getOrThrow(@NotNull NamespacedKey namespacedKey) {
        return Objects.requireNonNull(registry.get(namespacedKey));
    }

    @Override
    public @NotNull Stream<T> stream() {
        return registry.values().stream();
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return registry.values().iterator();
    }

    @Override
    public @Nullable T match(@NotNull String input) {
        return Registry.super.match(input);
    }
}
