package net.mathias2246.buildmc.util;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public abstract class ParticleSpawner extends BukkitRunnable {

    private int repeatTimes;
    public final int delay;

    public ParticleSpawner(int repeatTimes, int delay) {
        this.repeatTimes = repeatTimes;
        this.delay = delay;
    }

    public ParticleSpawner clone() throws CloneNotSupportedException {
        return (ParticleSpawner) super.clone();
    }

    public abstract void buildParticleSpawner();

    protected abstract boolean shouldStop();

    protected abstract void display();

    protected abstract void onStop();

    public @NotNull Player currentPlayer;

    @Override
    public void run() {
        if (repeatTimes < 0 || shouldStop()) this.cancel();

        display();
        repeatTimes--;
    }
}
