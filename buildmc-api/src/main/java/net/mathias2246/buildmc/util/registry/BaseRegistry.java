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

/// An implementation of bukkits {@link Registry} that is mutable.
/// <p>
/// You have to keep in mind that this registry type can be modified at any given time at runtime.
/// </p>
public class BaseRegistry<T extends Keyed> implements Registry<T> {

    private final HashMap<NamespacedKey, T> registry;

    public BaseRegistry() {
        this.registry = new HashMap<>();
    }

    /// Sets an entry with a {@link NamespacedKey} and value
    /// @throws NullPointerException if the key is {@code null}
    public @Nullable T set(@NotNull NamespacedKey namespacedKey, @NotNull T value) {
        return registry.put(Objects.requireNonNull(namespacedKey), value);
    }

    /// Checks if the given {@link NamespacedKey} is inside this registry or not.
    /// @throws NullPointerException if the key is {@code null}
    /// @return {@code true} if, registry contains key; else {@code false}
    public boolean contains(@NotNull NamespacedKey namespacedKey) {
        return registry.containsKey(Objects.requireNonNull(namespacedKey));
    }

    /// @return {@link Set} with all keys in this registry
    public Set<NamespacedKey> keySet() {
        return registry.keySet();
    }

    /// @return The amount of entries in this registry
    public int size() {
        return registry.size();
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
