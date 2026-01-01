package net.mathias2246.buildmc.api.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ItemUtil {
    /**A helper method for editing {@link ItemMeta} using a {@link Consumer} that takes in an {@link ItemMeta} instance.
     * <p>The edited {@link ItemMeta} instance will be automatically be set on the {@link ItemStack} instance after editing.</p>
     * <p>If the meta returned by {@code itemStack.getItemMeta();} equals null, nothing will happen.</p>
     *
     * @param consumer The consumer used to edit the {@link ItemMeta}
     * @param itemStack The {@link ItemStack} that should be edited
     */
    public static void editMeta(@NotNull ItemStack itemStack, @NotNull Consumer<@NotNull ItemMeta> consumer) {
        ItemMeta meta = itemStack.getItemMeta();

        if (meta == null) return;

        consumer.accept(meta);
        itemStack.setItemMeta(meta);
    }

    /**Will set the item name of the given {@link ItemStack} to the given {@link Component} name.
     * @param newName The new name of the item. When null, the name will be removed from the item.*/
    public static void setItemLegacyComponentName(@NotNull ItemStack itemStack, @Nullable Component newName) {
        editMeta(
                itemStack,
                (meta) -> {
                    if (newName == null) meta.setItemName(null);

                    else meta.setItemName(LegacyComponentSerializer.legacySection().serialize(newName));
                }
        );
    }
    /**Will create an {@link ItemStack} with the given {@link Component} name.
     * @param newName The new name of the item. When null, the name will be removed from the item.*/
    public static @NotNull ItemStack setItemLegacyComponentName(@NotNull Material material, @Nullable Component newName) {
        ItemStack i = new ItemStack(material);
        editMeta(
                i,
                (meta) -> {
                    if (newName == null) meta.setItemName(null);

                    else meta.setItemName(LegacyComponentSerializer.legacySection().serialize(newName));
                }
        );
        return i;
    }


    /**Will try to get a {@link AbstractCustomItem} from an {@link ItemStack} if it is a custom item.
     * @return The {@link AbstractCustomItem}, or {@code null} if not a custom item.
     * */
    public static @Nullable AbstractCustomItem getCustomItemFromItemStack(@NotNull ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return null;

        var customItemKey = AbstractCustomItem.getCustomItemKey(itemStack);

        if (customItemKey == null) return null;

        return AbstractCustomItem.customItemsRegistry.get(customItemKey);
    }

}
