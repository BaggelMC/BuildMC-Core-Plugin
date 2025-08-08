package net.mathias2246.buildmc.ui.elements;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class BaseElement {

    @FunctionalInterface
    public interface Builder<T extends BaseElement> {
        T build();
    }

    public static final @NotNull NamespacedKey ELEMENT_PDC_KEY = Objects.requireNonNull(NamespacedKey.fromString("buildmc:ui_element"));

    public static boolean isElement(@Nullable ItemStack item) {
        if (item == null) return false;
        if (item.getItemMeta() instanceof ItemMeta meta) {
            return meta.getPersistentDataContainer().has(ELEMENT_PDC_KEY);
        }
        return false;
    }

    public static @Nullable NamespacedKey getElementKey(@Nullable ItemStack item) {
        if (item == null) return null;
        if (item.getItemMeta() instanceof ItemMeta meta) {
            var pdc = meta.getPersistentDataContainer();
            var r = pdc.get(ELEMENT_PDC_KEY, PersistentDataType.STRING);
            if (r == null) return null;
            return NamespacedKey.fromString(r);
        }
        return null;
    }

    public final int slotID;

    protected BaseElement(int slotID) {
        this.slotID = slotID;
    }
}
