package net.mathias2246.buildmc.item;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

public record CustomItemListener(@NotNull CustomItemRegistry itemRegistry) implements Listener {

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onItemInteract(@NotNull PlayerInteractEvent event) {
        if (!(itemRegistry.getFromItemStack(event.getItem()) instanceof AbstractCustomItem item)) return;
        // Suppress DataFlowIssue because there is already null check in CustomItemRegistry.getFromItemStack
        //noinspection DataFlowIssue
        item.onInteractEvent(event.getItem(), event);
    }
}
