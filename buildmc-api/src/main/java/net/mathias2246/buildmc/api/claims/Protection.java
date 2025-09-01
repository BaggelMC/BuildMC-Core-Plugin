package net.mathias2246.buildmc.api.claims;

import net.mathias2246.buildmc.api.ui.Displayable;
import net.mathias2246.buildmc.util.DeferredRegistry;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Protection implements Keyed, Displayable, Listener {


    /**A {@link Collection} of all the default protection keys when creating a new claim.*/
    @ApiStatus.Internal
    public static final @NotNull Collection<String> defaultProtections = new ArrayList<>();

    public static boolean isHiddenProtection(@NotNull DeferredRegistry<Protection> registry, @Nullable NamespacedKey key) {
        if (key == null) return false;

        var o = registry.getOptional(key);

        AtomicBoolean is = new AtomicBoolean(false);

        o.ifPresent(
                (v) -> is.set(true)
        );

        return is.get();

    }

    private final @NotNull NamespacedKey key;

    private boolean isHidden;

    /**If this protection should be enabled by default when creating a new claim.
     *
     * @return True if, enabled by default in new claims.
     * */
    public boolean isDefaultEnabled() {
        return isDefaultEnabled;
    }

    /**Sets if this protection should be enabled by default in new claims.
     *
     * @param defaultEnabled The new default value
     * */
    public void setDefaultEnabled(boolean defaultEnabled) {
        isDefaultEnabled = defaultEnabled;
    }

    protected boolean isDefaultEnabled = true;

    public Protection(@NotNull NamespacedKey key, boolean defaultEnabled) {
        this.key = key;
        this.isDefaultEnabled = defaultEnabled;
    }
    public Protection(@NotNull NamespacedKey key, boolean defaultEnabled, boolean isHidden) {
        this.key = key;
        this.isDefaultEnabled = defaultEnabled;
        this.isHidden = isHidden;
    }


    public Protection(@NotNull NamespacedKey key) {
        this.key = key;
    }

     public abstract String getTranslationBaseKey();

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean hidden) {
        this.isHidden = hidden;
    }


    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }
}
