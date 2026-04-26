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

/**A {@link DeferredRegistry} is an implementation of bukkits {@link Registry} interface.
 * <p>A {@link DeferredRegistry} stays mutable before all plugins are loaded.
 * You can change the contents of the registry while BuildMC-Core is un-initialized.
 * After initializing using {@code initialize();}, the register will be made immutable for safety.</p>
 *
 * @see BaseRegistry
 * */
public class DeferredRegistry<T extends Keyed> implements Registry<T>, net.kyori.adventure.key.Keyed, Iterable<T> {

    private final Map<NamespacedKey, T> map = new HashMap<>();

    private final @NotNull Key regKey;

    public DeferredRegistry(@NotNull Key key) {
        this.regKey = key;
    }

    private boolean isInitialized = false;

    /**Checks if this deferred-registry is already initialized or not.
     *     <h4>Important</h4>
     *     When this deferred-registry is initialized, it will be turned immutable for safety.
     *
     * @return True if this deferred-registry instance is initialized.*/
    public boolean isInitialized() {
        return isInitialized;
    }

    /**Initializes this deferred-registry.
     *
     * <h4>Important</h4>
     *      When this deferred-registry is initialized, it will be turned immutable for safety.
     */
    public void initialize() {
        if (isInitialized) throw new IllegalArgumentException("A registry cannot be initialized twice!");
        isInitialized = true;
    }

    /**Registers a new entry under a certain key.
     * <p>
     *     Nothing will change when this deferred-registry is initialized.
     * </p>
     *
     * @param entry The entry to add
     *
     * @return The entry you tried to add
     * */
    @SuppressWarnings("UnusedReturnValue")
    public @NotNull T addEntry(@NotNull T entry) {
        if (isInitialized) return entry;
        map.putIfAbsent(entry.getKey(), entry);
        return entry;
    }

    /**Removes an entry this deferred-registry
     *
     * @param key The key of the entry to remove
     *
     * @throws IllegalStateException If this deferred-registry is already initialized.
     *
     * @throws IllegalArgumentException If this deferred-registry does not contain an entry under the given {@link NamespacedKey}.
     * */
    public void removeEntryOrThrow(@NotNull NamespacedKey key) throws IllegalStateException, IllegalArgumentException {
        if (isInitialized) throw new IllegalStateException("A registry cannot be changed after being initialized!");
        if (!map.containsKey(key)) throw new IllegalArgumentException("A registry cannot be changed after being initialized!");
        map.remove(key);
    }

    /**
     * Removes an entry this deferred-registry
     *
     * @param key The key of the entry to remove
     * */
    public void removeEntry(@NotNull NamespacedKey key) {
        if (isInitialized) return;
        map.remove(key);
    }

    /**
     * Registers multiple new entries.
     * <p>
     *     Nothing will change when this deferred-registry is initialized.
     * </p>
     *
     * @param entries The entry to add
     * */
    @SafeVarargs
    public final void addEntries(@NotNull T... entries) {
        if (isInitialized) return;
        for (var e : entries) {
            map.putIfAbsent(e.getKey(), e);
        }
    }

    /**Registers a new entry under a certain key.
     *
     * @param entry The entry to add
     *
     * @throws IllegalArgumentException when an entry is already registered under the same key.
     *
     * @throws IllegalStateException when this registry is already initialized
     * */
    public void addEntryOrThrow(@NotNull T entry) throws IllegalArgumentException, IllegalStateException {
        if (isInitialized) throw new IllegalStateException("This Registry is already initialized! You can only change this registry before it is initialized!");

        if (map.containsKey(entry.getKey())) throw new IllegalArgumentException("An entry with the same key already exists inside this registry!");
        map.put(entry.getKey(), entry);
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
        return map.get(namespacedKey);
    }

    /** Gets the {@link NamespacedKey} of a {@link T}
     * @param t The object to check
     *
     * @return The {@link NamespacedKey} of the object, or null if the parameter is null
     * **/
    @Override
    public @Nullable NamespacedKey getKey(@Nullable T t) {
        if (t == null) return null;
        return t.getKey();
    }

    private final @NotNull Map<TagKey<T>, Tag<T>> tags = new HashMap<>();

    /** Adds a {@link org.bukkit.Tag} to this registry, if not initialized.
     *
     * @param tag The {@link Tag} to add
     * **/
    public void addTag(@NonNull Tag<T> tag) {
        if (isInitialized) return;
        tags.put(tag.tagKey(), tag);
    }

    /** Removes a {@link org.bukkit.Tag} from this registry, if not initialized.
     *
     * @param tag The {@link TagKey} of the tag to remove
     * **/
    public void removeTag(@NonNull TagKey<T> tag) {
        if (isInitialized) return;
        tags.remove(tag);
    }

    /** Removes a {@link org.bukkit.Tag} from this registry, if not initialized.
     *
     * @param tag The {@link Tag} to remove
     * **/
    public void removeTag(@NonNull Tag<T> tag) {
        if (isInitialized) return;
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
        var tag = tags.get(tagKey);
        if (tag == null) throw new NullPointerException("The given tag key '" + tagKey + "' was not found in this registry.");
        return tag;
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
     *  Gets an entry from the deferred-registry, or throws an exception
     *
     * @param namespacedKey The {@link NamespacedKey} of the entry you are trying to retrieve
     *
     * @throws IllegalArgumentException when no entry is registered under the given {@link NamespacedKey}
     *
     * @return The entry under the given {@link NamespacedKey}.
     * */
    @Override
    public @NotNull T getOrThrow(@NotNull NamespacedKey namespacedKey) throws IllegalArgumentException {
        if (!map.containsKey(namespacedKey)) throw new IllegalArgumentException("The given key was not found");
        return map.get(namespacedKey);
    }

    /**
     * Optionally retrieves an entry from this deferred-registry by its {@link NamespacedKey}.
     * <h4>Usage</h4>
     * This method optionally returns an entry of this deferred-registry.<br>
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
        return Optional.ofNullable(map.get(namespacedKey));
    }

    /**Optionally retrieves an entry from this deferred-registry by its {@link NamespacedKey} in a {@link String} representation.
     * <p>
     *     When using an invalid {@link String} representation of a {@link NamespacedKey},
     *     an empty {@link Optional} will be returned.
     * </p>
     * <h4>Usage</h4>
     * This method optionally returns an entry of this deferred-registry.<br>
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
                Optional.ofNullable(map.get(key));
    }

    /**
     * @return A {@link Stream} containing all entries of this deferred-registry.
     * */
    @Override
    public @NotNull Stream<T> stream() {
        return map.values().stream();
    }

    /**
     * @return A {@link Stream} containing all keys in this deferred-registry.
     * */
    public @NotNull Stream<NamespacedKey> keyStream() {
        return map.keySet().stream();
    }

    /// @return The amount of entries in this registry
    @Override
    public int size() {
        return map.size();
    }

    /**
     * @return An {@link Iterator} for iterating over all entries inside this deferred-registry
     * */
    @Override
    public @NotNull Iterator<T> iterator() {
        return map.values().iterator();
    }

    /**
     * @return The {@link Key} of this registry
     * **/
    @Override
    public @NotNull Key key() {
        return regKey;
    }
}
