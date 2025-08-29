package net.mathias2246.buildmc.api;

import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.MainClass;
import net.mathias2246.buildmc.util.SoundManager;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class BuildMcAPIImpl implements BuildMcAPI {
    private final Plugin plugin;
    private final MainClass mainClass;

    public BuildMcAPIImpl(Plugin plugin, MainClass mainClass) {
        this.plugin = plugin;
        this.mainClass = mainClass;
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return plugin;
    }

    @Override
    public @NotNull MainClass getMainClass() {
        return mainClass;
    }

    @Override
    public @NotNull SoundManager getSoundManager() {
        return CoreMain.soundManager;
    }
}
