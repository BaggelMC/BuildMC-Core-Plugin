package net.mathias2246.buildmc.api.ui;

import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/** Used to define a contract for per-player {@link ItemStack} creation in UIs. **/
public interface Displayable {
    /** Create a new {@link ItemStack} for usage in UIs.
     *
     * <p>
     *     Requires a {@link Player} for server-side translations of names and item lore and other data.
     * </p>
     * @param uiHolder The {@link Player} that the item is show to
     * @param gui The {@link Gui} instance the item is used in
     *
     * @return The {@link ItemStack} to display to the player**/
    @NotNull ItemStack getDisplay(@NotNull Player uiHolder, @NotNull Gui gui);

    /** Get the click action associated with this display item.
     *
             * <p>
     *     Returning {@code null} indicates that the display item has no
     *     click handler and should not perform any action when clicked.
                * </p>
                *
                * @return The {@link Consumer} to invoke when the item is clicked,
                *         or {@code null} if no click action is defined
     */
    @Nullable Consumer<? super InventoryClickEvent> getDisplayAction();

}
