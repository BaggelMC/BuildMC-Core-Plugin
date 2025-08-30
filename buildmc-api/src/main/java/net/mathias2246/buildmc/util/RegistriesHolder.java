package net.mathias2246.buildmc.util;

import org.bukkit.Keyed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RegistriesHolder {

    private final Map<String, DeferredRegistry<? extends Keyed>> registries = new HashMap<>();

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


    public DeferredRegistry<?> get(@NotNull String key) {
        return registries.get(key);
    }

    public @Nullable <T extends Keyed> DeferredRegistry<T> addRegistry(@NotNull String key, @NotNull DeferredRegistry<T> registry) {
        if (registries.containsKey(key)) return null;

        registries.put(key, registry);
        return registry;
    }

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
