package net.mathias2246.buildmc.util;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class EnabledUtils
{
    @SafeVarargs
    public static <T> Set<T> of(
            @NotNull Enabled<Supplier<T>>... with
    ) {
        var set = new HashSet<T>();

        for (var kvp : with) {
            if (kvp.enabled) {
                set.add(kvp.value.get());
            }
        }

        return set;
    }



    public static class  Enabled <T>{
        public T value;
        public boolean enabled;

        public Enabled(T value, boolean enabled) {
            this.value = value;
            this.enabled = enabled;
        }

        public Enabled(T value, @Nullable ConfigurationSection enabled, boolean defaultEnabled) {
            this.value = value;
            this.enabled = enabled == null ? defaultEnabled : enabled.getBoolean("enabled", defaultEnabled);

        }
    }
}
