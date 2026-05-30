package net.mathias2246.buildmc.util;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**Base class for running certain actions periodically on a player.
 * <p>Usage is discouraged because there are better alternatives.</p>**/
@ApiStatus.Internal
public abstract class PlayerTimer extends BukkitRunnable {

    /// The current amount of steps the internal loop has stepped. <p>When higher than {@link PlayerTimer#steps} the Timer is exited.</p>
    public int currentStep;

    /// The amount of steps before the Timer is exited
    public final int steps;

    /// The number of ticks between each step
    public final int ticks;

    /// The {@link Player} that owns this timer
    public final @NotNull Player player;

    /// The {@link Plugin} that owns this timer
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
