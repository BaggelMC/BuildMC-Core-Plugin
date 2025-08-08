package net.mathias2246.buildmc.ui;

import net.mathias2246.buildmc.ui.elements.BaseElement;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**An abstract Inventory User-Interface.
 * <p>Can be used to set elements like buttons or similar UI features inside an inventory.</p>*/
public abstract class AbstractInventoryUI implements InventoryHolder {

    /**A Builder used to create new instances of {@link AbstractInventoryUI}s.*/
    @FunctionalInterface
    public interface Builder<T extends AbstractInventoryUI> extends Cloneable {
        /**Builds the {@link AbstractInventoryUI} from the settings given in this builder.*/
        T build();
    }


    private final @NotNull Inventory inventory;

    public final @NotNull Map<Integer, BaseElement> elements;

    protected AbstractInventoryUI(@NotNull Plugin plugin, int slotCount) {
        this.inventory = plugin.getServer().createInventory(this, slotCount);
        elements = new HashMap<>();
    }

    /**@return This UI's Inventory view.*/
    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
