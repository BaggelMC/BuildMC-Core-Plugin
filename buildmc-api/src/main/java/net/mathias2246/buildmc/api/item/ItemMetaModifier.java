package net.mathias2246.buildmc.api.item;

import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**An interface that defines ItemMeta modifications that are platform specific*/
public interface ItemMetaModifier {

    /**Modifies the given ItemMeta.
     * @param meta The ItemMeta to modify
     * @param player The player that was given in the context or null*/
    void modifyMeta(@NotNull ItemMeta meta, @Nullable Player player);

    /**Modifies the given ItemMeta.
     * @param meta The ItemMeta to modify
     * @param player The player that was given in the context or null*/
    void modifyMeta(@NotNull ItemMeta meta, @Nullable Player player, @Nullable Object arg);

}
