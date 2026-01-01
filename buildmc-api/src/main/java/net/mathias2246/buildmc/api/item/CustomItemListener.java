package net.mathias2246.buildmc.api.item;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

/**The Listener used to call the events inside custom item type implementations.
 * <p><b>NOTE: </b>This listener needs to be registered in your plugin when you want to be able to use custom items.</p>*/
public class CustomItemListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onItemInteract(@NotNull PlayerInteractEvent event) {
        if (event.getItem() == null) return;

        if (!(ItemUtil.getCustomItemFromItemStack(event.getItem()) instanceof AbstractCustomItem item)) return;

        item.onInteractEvent(event.getItem(), event);
    }
}
