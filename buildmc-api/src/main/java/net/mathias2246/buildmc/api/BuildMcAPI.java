package net.mathias2246.buildmc.api;

import net.mathias2246.buildmc.MainClass;
import net.mathias2246.buildmc.util.RegistriesHolder;
import net.mathias2246.buildmc.util.SoundManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

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
     * Uses a {@link Consumer} to change the contents of the core configuration before the core plugin finishes loading.
     *
     * @param consumer A consumer to apply changes to the core configuration
     */
    void editConfiguration(@NotNull Consumer<FileConfiguration> consumer);

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

    @NotNull
    RegistriesHolder getRegistriesHolder();
}
