package net.mathias2246.buildmc.ui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface Displayable {

    @NotNull GuiItem getDisplay(@NotNull Player uiHolder, @NotNull Gui gui);

}
