package net.mathias2246.buildmc.util;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Utility methods for safely scheduling tasks on the main server thread.
 * <p>
 * These helpers are primarily used to ensure event ordering consistency between
 * different Bukkit events that may fire within the same tick.
 * For example, {@code PlayerInteractEvent} may occur before {@code PlayerDropItemEvent},
 * so deferring logic to the next tick guarantees that all other synchronous
 * event handlers have completed first.
 * <p>
 * Usage example:
 * <pre>{@code
 * TaskUtil.defer(plugin, () -> {
 *     if (!ItemDropTracker.droppedRecently(player)) {
 *         handleToolUse(item, event);
 *     }
 * });
 * }</pre>
 */
@ApiStatus.Internal
public final class TaskUtil {

    private TaskUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Runs the given task on the next tick on the main server thread.
     * <p>
     * This is often used to delay execution until after all synchronous
     * event handlers for the current tick have completed.
     *
     * @param plugin the owning plugin instance
     * @param task   the task to execute
     */
    public static void defer(@NotNull Plugin plugin, @NotNull Consumer<ScheduledTask> task) {
        Bukkit.getGlobalRegionScheduler().run(plugin, task);
    }

    /**
     * Runs the given task on the next tick on the main server thread.
     * <p>
     * This is often used to delay execution until after all synchronous
     * event handlers for the current tick have completed.
     *
     * @param plugin the owning plugin instance
     * @param task   the task to execute
     */
    public static void deferForEntity(@NotNull Plugin plugin, @NotNull Entity entity, @NotNull Consumer<ScheduledTask> task) {
        entity.getScheduler().run(plugin, task, null);
    }

    /**
     * Runs the given task after a specified number of ticks on the main thread.
     *
     * @param plugin the owning plugin instance
     * @param delay  number of ticks to delay
     * @param task   the task to execute
     */
    public static void later(@NotNull Plugin plugin, long delay, @NotNull Consumer<ScheduledTask> task) {
        Bukkit.getGlobalRegionScheduler().runDelayed(plugin, task, delay);
    }
}
