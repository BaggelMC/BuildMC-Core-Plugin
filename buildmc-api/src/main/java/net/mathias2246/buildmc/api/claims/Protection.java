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

/**
 * Represents a type of {@code Protection} that can be applied to claims.
 * <p>
 * Protections define specific behaviors, rules, or restrictions within a claim (e.g., blocking explosions,
 * preventing PVP, etc.). Each protection is uniquely identified by a {@link NamespacedKey} and can be
 * enabled/disabled by default when a claim is created.
 * </p>
 *
 * <p>This class is abstract; concrete implementations should define
 * the translation key for the UI via {@link #getTranslationBaseKey()}.</p>
 */
public abstract class Protection implements Keyed, Displayable, Listener {

    /**
     * A collection of all default protection keys that should be automatically
     * applied when creating a new claim.
     * <p>
     * This field is internal and should not be modified directly outside of
     * registration logic.
     * </p>
     */
    @ApiStatus.Internal
    public static final @NotNull Collection<String> defaultProtections = new ArrayList<>();

    /**
     * Checks if a protection is considered a "hidden" protection in the given registry.
     * Hidden protections are usually not shown to players directly but may still be enforced internally.
     *
     * @param registry the registry of protections
     * @param key      the {@link NamespacedKey} of the protection to check, may be {@code null}
     * @return {@code true} if the protection exists in the registry and is marked hidden, otherwise {@code false}
     */
    public static boolean isHiddenProtection(@NotNull DeferredRegistry<Protection> registry, @Nullable NamespacedKey key) {
        if (key == null) return false;

        var o = registry.getOptional(key);

        AtomicBoolean is = new AtomicBoolean(false);

        o.ifPresent(
                (v) -> is.set(v.isHidden)
        );

        return is.get();
    }

    private final @NotNull NamespacedKey key;
    private boolean isHidden;
    protected boolean isDefaultEnabled = true;

    /**
     * Creates a new {@code Protection}.
     *
     * @param key           the unique {@link NamespacedKey} identifier of this protection
     * @param defaultEnabled whether this protection should be enabled by default for new claims
     */
    public Protection(@NotNull NamespacedKey key, boolean defaultEnabled) {
        this.key = key;
        this.isDefaultEnabled = defaultEnabled;
    }

    /**
     * Creates a new {@code Protection}.
     *
     * @param key           the unique {@link NamespacedKey} identifier of this protection
     * @param defaultEnabled whether this protection should be enabled by default for new claims
     * @param isHidden      whether this protection should be hidden from players
     */
    public Protection(@NotNull NamespacedKey key, boolean defaultEnabled, boolean isHidden) {
        this.key = key;
        this.isDefaultEnabled = defaultEnabled;
        this.isHidden = isHidden;
    }

    /**
     * Creates a new {@code Protection} with default values.
     * <p>Protections created this way default to {@code isDefaultEnabled = true} and {@code isHidden = false}.</p>
     *
     * @param key the unique {@link NamespacedKey} identifier of this protection
     */
    public Protection(@NotNull NamespacedKey key) {
        this.key = key;
    }

    /**
     * Gets the translation base key used for localization.
     * <p>This value should be used as the root for retrieving translatable messages for the UI.</p>
     *
     * @return the translation key base, never {@code null}
     */
    public abstract String getTranslationBaseKey();

    /**
     * Returns whether this protection is hidden from players.
     * <p>Hidden protections are still enforced but not visible in UIs or menus.</p>
     *
     * @return {@code true} if this protection is hidden, otherwise {@code false}
     */
    public boolean isHidden() {
        return isHidden;
    }

    /**
     * Sets whether this protection is hidden from players.
     *
     * @param hidden {@code true} to hide this protection, {@code false} to make it visible
     */
    public void setHidden(boolean hidden) {
        this.isHidden = hidden;
    }

    /**
     * Returns whether this protection should be enabled by default when creating a new claim.
     *
     * @return {@code true} if enabled by default, otherwise {@code false}
     */
    public boolean isDefaultEnabled() {
        return isDefaultEnabled;
    }

    /**
     * Sets whether this protection should be enabled by default in new claims.
     *
     * @param defaultEnabled {@code true} to enable by default, otherwise {@code false}
     */
    public void setDefaultEnabled(boolean defaultEnabled) {
        isDefaultEnabled = defaultEnabled;
    }

    /**
     * Returns the unique {@link NamespacedKey} for this protection.
     *
     * @return the key, never {@code null}
     */
    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }
}
