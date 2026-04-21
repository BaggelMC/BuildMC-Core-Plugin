package net.mathias2246.buildmc.api;

import net.mathias2246.buildmc.MainClass;
import net.mathias2246.buildmc.api.claims.ClaimManager;
import net.mathias2246.buildmc.api.endEvent.EndManager;
import net.mathias2246.buildmc.api.spawnEyltra.ElytraManager;
import net.mathias2246.buildmc.api.status.StatusManager;
import net.mathias2246.buildmc.util.SoundManager;
import net.mathias2246.buildmc.util.registry.RegistriesHolder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * The primary API interface for BuildMC.
 * <p>
 * External plugins can use this interface to interact with BuildMC's core systems,
 * access registries, modify behaviour, and hook into extension points.
 * </p>
 */
public interface BuildMcAPI {

    /** Tries to get the BuildMcAPI service from Bukkit.
     * <p>
     * This is required to merge the classpath of the API with our plugin to use our API in your plugin.
     * </p>
     * This will crash your plugin if the BuildMC-Core is not loaded before your extension.
     *
     * @return The {@link BuildMcAPI} instance or {@code null} if not loaded.
     * **/
    static @Nullable BuildMcAPI tryLoadAPI() {
        return Bukkit.getServicesManager().load(BuildMcAPI.class);
    }

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
     * Gets the {@link MainClass} instance of BuildMC.
     *
     * @return the main class instance
     */
    @NotNull
    MainClass getMainClass();

    /**
     * Gets the {@link SoundManager}
     *
     * @return the SoundManager instance
     */
    @NotNull
    SoundManager getSoundManager();

    /**
     * Gets the {@link ClaimManager}
     *
     * @return the ClaimManager instance
     */
    ClaimManager getClaimManager();

    /**
     * Gets the {@link EndManager}
     *
     * @return the EndManager instance
     */
    EndManager getEndManager();

    /**
     * Gets the {@link ElytraManager}
     *
     * @return the ElytraManager instance
     */
    ElytraManager getElytraManager();

    /**
     * Gets the {@link StatusManager}
     *
     * @return the ElytraManager instance
     */
    StatusManager getStatusManager();

    /**
     * Gets the shared {@link RegistriesHolder} that contains all custom registries shared over all plugins that use the BuildMC-API.
     * <p>
     *     This can be used to store a reference to any type that implements Bukkit's {@link org.bukkit.Registry} interface.
     * </p>
     *
     * @return The shared {@link RegistriesHolder} instance of the Core
     */
    @NotNull
    RegistriesHolder getRegistriesHolder();
}
