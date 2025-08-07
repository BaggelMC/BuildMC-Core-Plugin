package net.mathias2246.buildmc.util;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public abstract class ParticleSpawner extends BukkitRunnable {

    public interface Builder<T extends ParticleSpawner> {
        T build(@NotNull Player player);
    }

    private int repeatTimes;
    public final int delay;

    public ParticleSpawner(int repeatTimes, int delay, @NotNull Player source) {
        this.repeatTimes = repeatTimes;
        this.delay = delay;
        this.source = source;

        buildParticleSpawner();
    }

    public abstract void buildParticleSpawner();

    protected abstract boolean shouldStop();

    protected abstract void display();

    protected abstract void onStop();

    protected @NotNull Player source;

    @Override
    public void run() {
        if (repeatTimes < 0 || shouldStop()) {
            onStop();
            this.cancel();
        }

        display();
        repeatTimes--;
    }
}
