package net.mathias2246.buildmc.util.registry;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

/// An implementation of bukkits {@link Registry} that is mutable.
/// <p>
/// You have to keep in mind that this registry type can be modified at any given time at runtime.
/// </p>
public class BaseRegistry<T extends Keyed> implements Registry<T>, Iterable<T> {

    private final HashMap<NamespacedKey, T> registry;

    public BaseRegistry() {
        this.registry = new HashMap<>();
    }

    /**Registers a new entry under a certain key.
     * <p>
     *     Nothing will change when this deferred-registry is initialized.
     * </p>
     *
     * @param entry The entry to add
     *
     * @return The entry you tried to add
     *
     * @throws NullPointerException if the key is {@code null} */
    @SuppressWarnings("UnusedReturnValue")
    public @NotNull T addEntry(@NotNull T entry) {
        registry.put(entry.getKey(), entry);
        return entry;
    }

    /**
     * Registers multiple new entries.
     *
     * @param entries The entry to add
     * */
    @SafeVarargs
    public final void addEntries(@NotNull T... entries) {
        for (var e : entries) {
            registry.putIfAbsent(e.getKey(), e);
        }
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

    /**
     *  Gets an entry from this registry, or throws an exception
     *
     * @param namespacedKey The {@link NamespacedKey} of the entry you are trying to retrieve
     *
     * @throws IllegalArgumentException when no entry is registered under the given {@link NamespacedKey}
     *
     * @return The entry under the given {@link NamespacedKey}.
     * */
    @Override
    public @NotNull T getOrThrow(@NotNull NamespacedKey namespacedKey) {
        return Objects.requireNonNull(registry.get(namespacedKey));
    }

    /**
     * @return A {@link Stream} containing all entries of this registry.
     * */
    @Override
    public @NotNull Stream<T> stream() {
        return registry.values().stream();
    }

    /**
     * @return A {@link Stream} containing all keys in this registry.
     * */
    public @NotNull Stream<NamespacedKey> keyStream() {
        return registry.keySet().stream();
    }

    /**
     * @return An {@link Iterator} for iterating over all entries inside this registry
     * */
    @Override
    public @NotNull Iterator<T> iterator() {
        return registry.values().iterator();
    }

    @Override
    public @Nullable T match(@NotNull String input) {
        return Registry.super.match(input);
    }

    /**
     * Optionally retrieves an entry from this registry by its {@link NamespacedKey}.
     * <h4>Usage</h4>
     * This method optionally returns an entry of this registry.<br>
     * You could use the returned {@link Optional} like this:
     * <pre>{@code
     * registry.getOptional(NamespacedKey.fromString("my_plugin:foo_entry"))
     *      .ifPresent(
     *          (entry) -> {
     *              entry.doSomething(); // You can do anything here
     *          }
     *      );
     * }</pre>
     *
     * @param namespacedKey The {@link NamespacedKey} of the entry you are trying to retrieve
     *
     * @return The {@link Optional} that might contain the entry from the given key
     * */
    public @NotNull Optional<T> getOptional(@NotNull NamespacedKey namespacedKey) {
        return Optional.ofNullable(registry.get(namespacedKey));
    }

    /**Optionally retrieves an entry from this registry by its {@link NamespacedKey} in a {@link String} representation.
     * <p>
     *     When using an invalid {@link String} representation of a {@link NamespacedKey},
     *     an empty {@link Optional} will be returned.
     * </p>
     * <h4>Usage</h4>
     * This method optionally returns an entry of this registry.<br>
     * You could use the returned {@link Optional} like this:
     * <pre>{@code
     * registry.getOptional("my_plugin:foo_entry")
     *      .ifPresent(
     *          (entry) -> {
     *              entry.doSomething(); // You can do anything here
     *          }
     *      );
     * }</pre>
     *
     * @param keyString The {@link String} representation of key of the entry you are trying to retrieve
     *
     * @return The {@link Optional} that might contain the entry from the given key
     * */
    public @NotNull Optional<T> getOptional(@NotNull String keyString) {
        NamespacedKey key = NamespacedKey.fromString(keyString);
        if (key == null) return Optional.empty();
        return
                Optional.ofNullable(registry.get(key));
    }
}
