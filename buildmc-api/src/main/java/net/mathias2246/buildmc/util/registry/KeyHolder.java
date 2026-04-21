package net.mathias2246.buildmc.util.registry;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

/**
 * A Helper class for storing an object instance with a {@link NamespacedKey} as identifier.
 * <p>
 * This object can then be used for storing a non-{@link Keyed} object instance inside a {@link org.bukkit.Registry}.
 * </p>
 *
 * @param <T> The type of the value of this {@link KeyHolder}
 *
 * @see Keyed
 * **/
public class KeyHolder<T> implements Keyed {

    private final @NotNull T value;
    private final @NotNull NamespacedKey key;

    /** Default constructor for creating a basic non-null {@link KeyHolder} instance.
     *
     * @param key The namespaced identifier for the instance of type {@link T}
     * @param value The actual object of type {@link T} that should have a namespaced identifier
     * **/
    public KeyHolder(@NotNull NamespacedKey key, @NotNull T value) {
        this.key = key;
        this.value = value;
    }

    /// Gets the stored object
    public @NotNull T getValue() {
        return value;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }
}
