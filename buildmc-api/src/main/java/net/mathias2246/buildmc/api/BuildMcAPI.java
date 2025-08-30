package net.mathias2246.buildmc.api;

import net.mathias2246.buildmc.MainClass;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.util.DeferredRegistry;
import net.mathias2246.buildmc.util.SoundManager;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * The primary API interface for BuildMC.
 * <p>
 * External plugins can use this interface to interact with BuildMC's core systems,
 * access registries, modify behavior, and hook into extension points.
 * </p>
 */
public interface BuildMcAPI {

    /**
     * Gets the Bukkit plugin instance of BuildMC.
     *
     * @return the plugin instance
     */
    @NotNull
    Plugin getPlugin();

    /**
     * Gets the main class instance of BuildMC.
     *
     * @return the main class instance
     */
    @NotNull
    MainClass getMainClass();

    /**
     * Gets the SoundManager used by BuildMC.
     *
     * @return the SoundManager instance
     */
    @NotNull
    SoundManager getSoundManager();

    @NotNull DeferredRegistry<Protection> getProtectionsRegistry();
}
