package net.mathias2246.buildmc.util;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public abstract class PlayerTimer extends BukkitRunnable {

    public int currentStep;

    public final int steps;

    public final int ticks;

    public final @NotNull Player player;

    public final @NotNull Plugin plugin;

    public PlayerTimer(@NotNull Plugin plugin, @NotNull Player player, int steps, int ticks) {
        this.steps = steps;
        this.player = player;
        this.ticks = ticks;
        this.plugin = plugin;
    }

    public void start(int tickDelay) {
        init();
        if (steps <= 0) {
            onExit();
        }
        this.runTaskTimer(plugin, tickDelay, ticks);
    }

    public abstract void onExit();

    protected abstract void init();

    protected abstract boolean shouldCancel();

    protected abstract void onCancel();

    protected abstract void onStep();

    @Override
    public void run() {
        currentStep++;
        if (shouldCancel()) {
            this.cancel();
            currentStep = 0;
            onCancel();
            return;
        } else if (currentStep > steps) {
            onExit();
            this.cancel();
            currentStep = 0;
            return;
        }

        onStep();
    }
}
