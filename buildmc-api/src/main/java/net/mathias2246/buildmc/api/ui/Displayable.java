package net.mathias2246.buildmc.api.ui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** Used to define a contract for per-player {@link GuiItem} creation in UIs. **/
@FunctionalInterface
public interface Displayable {
    /** Create a new {@link GuiItem} for usage in UIs.
     *
     * <p>
     *     Requires a {@link Player} for server-side translations of names and item lore and other data.
     * </p>
     * @param uiHolder The {@link Player} that the item is show to
     * @param gui The {@link Gui} instance the item is used in
     *
     * @return The {@link GuiItem} to display to the player**/
    @NotNull GuiItem getDisplay(@NotNull Player uiHolder, @NotNull Gui gui);

}
