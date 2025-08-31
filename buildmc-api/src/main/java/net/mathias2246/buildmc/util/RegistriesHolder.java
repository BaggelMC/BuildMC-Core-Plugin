package net.mathias2246.buildmc.util;

import org.bukkit.Keyed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**A registry-holder stores a collection of {@link DeferredRegistry} instances under a certain key.
 *
 * <p>
 * BuildMC and Extension plugins should all register <i>static or type</i> instances inside DeferredRegistries.<br>
 * Storing certain types in registries allows other plugins to access them and add or remove entries before the server finishes loading all plugins.
 * </p>
 * <p>
 *     Other extension plugins can retrieve the BuildMC registry-holder through the api using {@code api.getRegistriesHolder();}
 * </p>
 */
public class RegistriesHolder {

    private final Map<String, DeferredRegistry<? extends Keyed>> registries = new HashMap<>();

    /**A Builder class used to create new instances of {@link RegistriesHolder}s.*/
    public static class Builder {

        private final RegistriesHolder holder = new RegistriesHolder();

        public Builder() {}

        public Builder addRegistry(@NotNull String key, @NotNull DeferredRegistry<? extends Keyed> registry) {
            if (holder.registries.containsKey(key)) return this;

            holder.registries.put(key, registry);
            return this;
        }

        public RegistriesHolder build() { return holder; }
    }

    private RegistriesHolder() {}

    /**Retrieves a {@link DeferredRegistry} instance from this holder by its key.
     *
     * @return The {@link DeferredRegistry} instance stored under the given key, or null if not existing.*/
    public @Nullable DeferredRegistry<?> get(@NotNull String key) {
        return registries.get(key);
    }

    /**
     * Adds a {@link DeferredRegistry} to this holder under a certain key.
     */
    public <T extends Keyed> void addRegistry(@NotNull String key, @NotNull DeferredRegistry<T> registry) {
        if (registries.containsKey(key)) return;

        registries.put(key, registry);
    }

    /**Optionally retrieves a {@link DeferredRegistry} instance from this holder by its key.
     * <h4>Usage</h4>
     * This method optionally returns a {@link DeferredRegistry}.<br>
     * You could use the returned {@link Optional} like this:
     * <pre>{@code
     * holder.getOptional("some_registry")
     *      .ifPresent(
     *          (registry) -> {
     *              registry.addEntry(...); // You can do anything here
     *          }
     *      );
     * }</pre>
     * */
    @SuppressWarnings("unchecked")
    public <T extends Keyed> Optional<DeferredRegistry<T>> getOptional(@Nullable String key) {
        if (key == null) return Optional.empty();

        @Nullable DeferredRegistry<T> r;
        try {
            r = (DeferredRegistry<T>) registries.get(key);
        } catch (Exception e) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                r
        );
    }
}
