package net.mathias2246.buildmc.util;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

/**A {@link DeferredRegistry} is an implementation of bukkits {@link Registry} interface.
 * <p>A {@link DeferredRegistry} allows you to change the contents of the registry while un-initialized.
 * After initializing using {@code initialize();}, the register will be made read-only for safety.</p>*/
public class DeferredRegistry<T extends Keyed> implements Registry<T> {

    private final Map<NamespacedKey, T> map = new HashMap<>();

    public boolean isInitialized = false;

    public boolean isInitialized() {
        return isInitialized;
    }

    public void initialize() {
        if (isInitialized) throw new IllegalArgumentException("A registry cannot be initialized twice!");
        isInitialized = true;
    }

    public void addEntry(@NotNull T entry) {
        if (isInitialized) return;
        map.putIfAbsent(entry.getKey(), entry);
    }

    public void addEntryOrThrow(@NotNull T entry) {
        if (isInitialized) throw new IllegalArgumentException("This Registry is already initialized! You can only change this registry before it is initialized!");

        if (map.containsKey(entry.getKey())) throw new IllegalArgumentException("An entry with the same key already exists inside this registry!");
        map.put(entry.getKey(), entry);
    }

    @Override
    public @Nullable T get(@NotNull NamespacedKey namespacedKey) {
        return map.get(namespacedKey);
    }

    @Override
    public @NotNull T getOrThrow(@NotNull NamespacedKey namespacedKey) {
        if (!map.containsKey(namespacedKey)) throw new IllegalArgumentException("The given key was not found");
        return map.get(namespacedKey);
    }

    @Override
    public @NotNull Stream<T> stream() {
        return map.values().stream();
    }

    public @NotNull Stream<NamespacedKey> keyStream() {
        return map.keySet().stream();
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return map.values().iterator();
    }
}
