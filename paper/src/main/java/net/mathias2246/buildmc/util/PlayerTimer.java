package net.mathias2246.buildmc.util;

import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

/**
 * Do Not use the {@link BukkitRunnable} methods like {@link BukkitRunnable#runTaskTimer(Plugin, long, long)}!
 * Only use the method implementations of this abstract class that wrap around the runnable!
 * **/
public abstract class PlayerTimer extends BukkitRunnable {

    public int currentStep;

    public final int steps;

    public final int ticks;

    public final @NotNull Player player;
    private final @NotNull EntityScheduler scheduler;
    private ScheduledTask task;

    public final @NotNull Plugin plugin;

    public PlayerTimer(@NotNull Plugin plugin, @NotNull Player player, int steps, int ticks) {
        this.steps = steps;
        this.player = player;
        scheduler = player.getScheduler();
        this.ticks = ticks;
        this.plugin = plugin;
    }

    public void start(int tickDelay) {
        init();
        if (steps <= 0) {
            onExit();
        }
        task = scheduler.runAtFixedRate(plugin, (scheduledTask) -> run(), null, 1, ticks);
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
            task.cancel();
            currentStep = 0;
            onCancel();
            return;
        } else if (currentStep > steps) {
            onExit();
            task.cancel();
            currentStep = 0;
            return;
        }

        onStep();
    }
}
