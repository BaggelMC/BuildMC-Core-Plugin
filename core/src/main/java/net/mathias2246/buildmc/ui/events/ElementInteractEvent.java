package net.mathias2246.buildmc.ui.events;

import net.mathias2246.buildmc.ui.elements.BaseElement;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.jetbrains.annotations.NotNull;

public class ElementInteractEvent extends ElementEvent {

    private final @NotNull InventoryInteractEvent interactEvent;

    public ElementInteractEvent(@NotNull BaseElement element, @NotNull InventoryInteractEvent interactEvent) {
        super(element);
        this.interactEvent = interactEvent;
    }

    public @NotNull InventoryInteractEvent getInteractEvent() {
        return interactEvent;
    }

    private static final @NotNull HandlerList HANDLERS = new HandlerList();

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
