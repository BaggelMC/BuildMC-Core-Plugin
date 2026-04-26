package net.mathias2246.buildmc.util.registry;

import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.key.Key;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.stream.Stream;

/// An implementation of Bukkit's {@link Registry} that is permanently mutable.
/// <p>
/// You have to keep in mind that entries in this registry type can be modified at after plugin initialization.
/// </p>
///
/// @see DeferredRegistry
public class BaseRegistry<T extends Keyed> implements Registry<T>, Iterable<T>, net.kyori.adventure.key.Keyed {

    private final HashMap<NamespacedKey, T> registry;

    private final @NotNull Key regKey;

    /// Initializes a new empty register
    public BaseRegistry(@NotNull Key key) {
        regKey = key;
        registry = new HashMap<>();
    }

    /**Removes an entry from the registry.

     * @param key The {@link NamespacedKey} of the entry to remove
     */
    public void removeEntry(@NotNull NamespacedKey key) {
        registry.remove(key);
    }

    /**
     * Removes all the entries from this registry.
     * The registry will be empty after this.
     */
    public void clear() {
        registry.clear();
    }

    /**Registers a new entry under a certain key.
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

    /**
     * Gets an entry from the deferred-registry
     *
     * @param namespacedKey The {@link NamespacedKey} of the entry you are trying to retrieve
     *
     * @return The entry under the given {@link NamespacedKey} or null if not found.
     * */
    @Override
    public @Nullable T get(@NotNull NamespacedKey namespacedKey) {
        return registry.get(namespacedKey);
    }

    /** Gets the {@link NamespacedKey} of a {@link T}
     * @param t The object to check
     *
     * @return The {@link NamespacedKey} of the object, or null if the parameter is null
     * **/
    @Override
    public NamespacedKey getKey(T t) {
        return t.getKey();
    }

    private final @NotNull Map<TagKey<T>, Tag<T>> tags = new HashMap<>();

    /** Adds a {@link org.bukkit.Tag} to this registry.
     *
     * @param tag The {@link Tag} to add
     * **/
    public void addTag(@NonNull Tag<T> tag) {
        tags.put(tag.tagKey(), tag);
    }

    /** Removes a {@link org.bukkit.Tag} from this registry.
     *
     * @param tag The {@link TagKey} of the tag to remove
     * **/
    public void removeTag(@NonNull TagKey<T> tag) {
        tags.remove(tag);
    }

    /** Removes a {@link org.bukkit.Tag} from this registry.
     *
     * @param tag The {@link Tag} to remove
     * **/
    public void removeTag(@NonNull Tag<T> tag) {
        tags.remove(tag.tagKey());
    }

    /** Checks if a certain {@link TagKey} is found for this registry.
     *
     * @param tagKey The key to check for
     * @return True if the tag key was found; Otherwise false
     * **/
    @Override
    public boolean hasTag(@NonNull TagKey<T> tagKey) {
        return tags.containsKey(tagKey);
    }

    /** Gets a {@link Tag} from this registry by its key.
     *
     * @throws NullPointerException Thrown when the registry doesn't contain any tag with the key that was given
     * @param tagKey The key of the tag
     * @return A {@link Tag} instance
     * **/
    @Override
    public @NonNull Tag<T> getTag(@NonNull TagKey<T> tagKey) {
        return tags.get(tagKey);
    }

    /** Gets a {@link Collection} containing all {@link Tag}s in this registry.
     *
     * @return A {@link Collection} containing all {@link Tag}s in this registry
     * **/
    @Override
    public @NonNull Collection<Tag<T>> getTags() {
        return tags.values();
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

    /**
     * @return The {@link Key} of this registry
     * **/
    @Override
    public @NotNull Key key() {
        return regKey;
    }
}
