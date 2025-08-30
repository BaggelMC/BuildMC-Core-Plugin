package net.mathias2246.buildmc.api.claims;

import net.mathias2246.buildmc.api.ui.Displayable;
import net.mathias2246.buildmc.util.DeferredRegistry;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public abstract class Protection implements Keyed, Displayable, Listener {

    public static final @NotNull DeferredRegistry<Protection> protections = new DeferredRegistry<>();

    // TODO: Needs to update when changing the protections registry
    public static final @NotNull Collection<String> defaultProtections = new ArrayList<>();

    public static boolean isHiddenProtection(@Nullable NamespacedKey key) {
        if (key == null) return false;

        return protections.stream()
                .anyMatch(
                        (p) -> p.isHidden
                );
    }

    private final @NotNull NamespacedKey key;

    private boolean isHidden;

    public boolean isDefaultEnabled() {
        return isDefaultEnabled;
    }

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
