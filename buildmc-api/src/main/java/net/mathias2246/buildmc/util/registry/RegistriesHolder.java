package net.mathias2246.buildmc.util.registry;

import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**A registry-holder stores a collection of {@link Registry} instances under a certain key.
 *
 * <p>
 * BuildMC and Extension plugins should all register <i>static or type</i> instances inside registries of some kind.<br>
 * Storing certain types in registries allows other plugins to access them and add or remove entries.
 * </p>
 * <p>
 *     Other extension plugins can retrieve the BuildMC registry-holder through the api using {@code api.getRegistriesHolder();}
 * </p>
 */
public class RegistriesHolder implements Iterable<Registry<? extends Keyed>> {

    private final @NotNull Map<String, Registry<? extends Keyed>> registries = new HashMap<>();

    /**@return A {@link Collection} containing all registries in this holder.*/
    public @NotNull Collection<Registry<? extends Keyed>> getCollection() {
        return registries.values();
    }

    @Override
    public @NotNull Iterator<Registry<? extends Keyed>> iterator() {
        return registries.values().iterator();
    }

    /**A Builder class used to create new instances of {@link RegistriesHolder}s.*/
    public static class Builder {

        private final RegistriesHolder holder = new RegistriesHolder();

        public Builder() {}

        /**Adds an existing {@link Registry} instance to the holder instance that this {@code Builder} will build.*/
        public @NotNull Builder addRegistry(@NotNull String key, @NotNull Registry<? extends Keyed> registry) {
            if (holder.registries.containsKey(key)) return this;

            holder.registries.put(key, registry);
            return this;
        }

        /**@return a new {@link RegistriesHolder} with all configured values*/
        public @NotNull RegistriesHolder build() { return holder; }
    }

    private RegistriesHolder() {}

    /**Retrieves a {@link Registry} instance from this holder by its key.
     *
     * @return The {@link Registry} instance stored under the given key, or null if not existing.*/
    public @Nullable Registry<?> get(@NotNull String key) {
        return registries.get(key);
    }

    /**Retrieves a {@link Registry} instance from this holder by its key.
     *
     * @return The {@link Registry} instance stored under the given key, or null if not existing.*/
    @SuppressWarnings("unchecked")
    public @Nullable <T extends Keyed> Registry<T> getAsType(@NotNull String key) {
        try {
            return (Registry<T>) registries.get(key);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Adds a {@link Registry} to this holder under a certain key.
     * @return The {@link Registry} instance that was passed in as a parameter
     */
    public <T extends Keyed> @NotNull Registry<T> addRegistry(@NotNull String key, @NotNull Registry<T> registry) {
        if (registries.containsKey(key)) return registry;

        registries.put(key, registry);
        return registry;
    }

    /**Optionally retrieves a {@link Registry} instance from this holder by its key and with a certain type.
     * <h4>Usage</h4>
     * This method optionally returns a {@link Registry}.<br>
     * You could use the returned {@link Optional} like this:
     * <pre>{@code
     * holder.getOptional("some_registry")
     *      .ifPresent(
     *          (registry) -> {
     *              registry.addEntry(...); // You can do anything here
     *          }
     *      );
     * }</pre>
     *
     * @return The {@link Optional} that contains the {@link Registry} that was found, or {@code null} if not*/
    @SuppressWarnings("unchecked")
    public <T extends Keyed> @NotNull Optional<Registry<T>> getOptional(@Nullable String key) {
        if (key == null) return Optional.empty();

        @Nullable Registry<T> r;
        try {
            r = (Registry<T>) registries.get(key);
        } catch (Exception e) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                r
        );
    }
}
