package net.mathias2246.buildmc.claims;

import net.mathias2246.buildmc.ui.Displayable;
import net.mathias2246.buildmc.util.DeferredRegistry;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public abstract class Protection implements Keyed, Displayable, Listener {

    public static final @NotNull DeferredRegistry<Protection> protections = new DeferredRegistry<>();

    public static final @NotNull Collection<NamespacedKey> defaultProtections = new ArrayList<>();


    private final @NotNull NamespacedKey key;

    private boolean isHidden;

    public boolean isDefaultEnabled() {
        return isDefaultEnabled;
    }

    public void setDefaultEnabled(boolean defaultEnabled) {
        isDefaultEnabled = defaultEnabled;
    }

    protected boolean isDefaultEnabled = true;

    public Protection(@NotNull NamespacedKey key) {
        this.key = key;
    }

     public abstract String getTranslationBaseKey();

    public boolean isHidden() {
        return isHidden;
    }


    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }
}
