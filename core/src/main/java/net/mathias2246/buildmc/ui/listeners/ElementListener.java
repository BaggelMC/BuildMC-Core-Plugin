package net.mathias2246.buildmc.ui.listeners;

import net.mathias2246.buildmc.ui.AbstractInventoryUI;
import net.mathias2246.buildmc.ui.elements.BaseElement;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record ElementListener(@NotNull AbstractInventoryUI inventoryUI) implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onClick(InventoryClickEvent event) {
        if (!Objects.equals(event.getInventory().getHolder(), inventoryUI)) return;

        var item = event.getCurrentItem();
        if (!BaseElement.isElement(item)) return;


    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onDrag(InventoryDragEvent event) {

    }
}
