package net.mathias2246.buildmc.util.registry;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

/**
 * A Helper class for storing an object instance with a {@link NamespacedKey}.
 * <p>
 * This object can then be used for storing a non-{@link Keyed} instance inside a {@link org.bukkit.Registry}.
 * </p>
 * **/
@SuppressWarnings("ClassCanBeRecord")
public class KeyHolder<T> implements Keyed {

    private final @NotNull T value;
    private final @NotNull NamespacedKey key;

    public KeyHolder(@NotNull NamespacedKey key, @NotNull T value) {
        this.key = key;
        this.value = value;
    }

    public @NotNull T getValue() {
        return value;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }
}
