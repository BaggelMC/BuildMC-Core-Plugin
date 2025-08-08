package net.mathias2246.buildmc.ui.events;

import net.mathias2246.buildmc.ui.elements.BaseElement;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**The base-class for all elements that have something to do with UI elements.*/
public abstract class ElementEvent extends Event {

    private final @NotNull BaseElement element;

    public ElementEvent(@NotNull BaseElement element) {
        this.element = element;
    }

    /**@return The Element that is referenced in this event.*/
    public @NotNull BaseElement getElement() {
        return element;
    }
}
