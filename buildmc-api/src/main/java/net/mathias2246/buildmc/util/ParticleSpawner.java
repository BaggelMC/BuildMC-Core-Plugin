package net.mathias2246.buildmc.util;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**A class for defining custom per-player particle spawners.
 * <p>
 *      These particles are only visible to the given player,
 *      and are spawned a set amount of times with a certain delay.
 * </p> */
public abstract class ParticleSpawner {

    /** The builder used to create a certain type of ParticleSpawner. **/
    @FunctionalInterface
    public interface Builder<T extends ParticleSpawner> {
        @Contract("_ -> new")
        T build(@NotNull Player player);
    }

    private final @NotNull Plugin plugin;

    private int repeatTimes;
    /** The delay in ticks between each run. */
    public final int delay;

    public ParticleSpawner(@NotNull Plugin plugin, int repeatTimes, int delay, @NotNull Player source) {
        this.plugin = plugin;

        this.repeatTimes = repeatTimes;
        this.delay = delay;
        this.source = source;

        buildParticleSpawner();
    }

    @SuppressWarnings("EmptyMethod")
    public abstract void buildParticleSpawner();

    /** Contains conditions for when to exit the run loop early.
     * <p>When this method returns true, the BukkitRunnable will stop.</p> */
    protected abstract boolean shouldStop();

    /** Displays the actual particles. **/
    protected abstract void display();

    /** Extra logic to be executed when the BukkitRunnable stops.
     * <p>E.g. removing metadata, or similar</p> */
    protected abstract void onStop();

    protected final @NotNull Player source;

    public void run() {
        source.getScheduler().runAtFixedRate(
                plugin,
                scheduledTask -> {
                    if (repeatTimes < 0 || shouldStop()) {
                        onStop();
                        scheduledTask.cancel();
                    }

                    display();
                    repeatTimes--;
                },
                null,
                1,
                delay
        );

    }
}
