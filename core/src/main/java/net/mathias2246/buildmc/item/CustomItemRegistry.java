package net.mathias2246.buildmc.item;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

/**A Register that stores AbstractCustomItem Instanced to be used somewhere else in your plugin.*/
public class CustomItemRegistry implements Registry<AbstractCustomItem> {

    private final Map<NamespacedKey, AbstractCustomItem> items = new LinkedHashMap<>();

    /**Registers a AbstractCustomItem Instance inside this registry.
    * @throws IllegalArgumentException When this key already exists inside this registry*/
    public void register(@NotNull AbstractCustomItem item) throws IllegalArgumentException {
        NamespacedKey key = item.getKey();
        if (items.containsKey(key)) {
            throw new IllegalArgumentException("Custom item with key " + key + " is already registered.");
        }
        items.put(key, item);
    }

    /**Registers a AbstractCustomItem Instance inside this registry.
     * This method does not throw an Exception when the key already exists inside this registry.*/
    public void tryRegister(@NotNull AbstractCustomItem item) {
        NamespacedKey key = item.getKey();
        if (items.containsKey(key)) return;
        items.put(key, item);
    }

    /**@return AbstractCustomItem instance from the registry or null if not found*/
    @Override
    public @Nullable AbstractCustomItem get(@Nullable NamespacedKey namespacedKey) {
        if (namespacedKey == null) return null;
        return items.get(namespacedKey);
    }

    /**Helper method for getting AbstractCustomItem Instance from ItemStack.
     * @return The AbstractCustomItem instance represented inside the ItemStack or null if not found*/
    public @Nullable AbstractCustomItem getFromItemStack(@Nullable ItemStack item) {
        return get(AbstractCustomItem.getCustomItemKey(item));
    }

    /**Tries to get a registry entry by key or throws an IllegalArgumentException.
     * @throws IllegalArgumentException When the given key does not exist inside the registry*/
    @Override
    public @NotNull AbstractCustomItem getOrThrow(@NotNull NamespacedKey namespacedKey) throws IllegalArgumentException {
        AbstractCustomItem item = items.get(namespacedKey);
        if (item == null) {
            throw new IllegalArgumentException("No custom item registered for key " + namespacedKey);
        }
        return item;
    }

    @Override
    public @NotNull Stream<AbstractCustomItem> stream() {
        return items.values().stream();
    }

    @Override
    public @NotNull Iterator<AbstractCustomItem> iterator() {
        return items.values().iterator();
    }

    public boolean contains(@NotNull NamespacedKey key) {
        return items.containsKey(key);
    }

    public int size() {
        return items.size();
    }
}