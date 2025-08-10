package net.mathias2246.buildmc.item;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

/**The Listener used to call the events inside custom item type implementations.
 * <p><b>NOTE: </b>This listener needs to be registered in your plugin when you want to be able to use custom items.</p>*/
public record CustomItemListener(@NotNull CustomItemRegistry itemRegistry) implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onItemInteract(@NotNull PlayerInteractEvent event) {
        if (!(itemRegistry.getFromItemStack(event.getItem()) instanceof AbstractCustomItem item)) return;
        // Suppress DataFlowIssue because there is already null check in CustomItemRegistry.getFromItemStack
        //noinspection DataFlowIssue
        item.onInteractEvent(event.getItem(), event);
    }
}
