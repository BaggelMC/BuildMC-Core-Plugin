package net.mathias2246.buildmc.claims.protections;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import net.mathias2246.buildmc.api.item.ItemUtil;
import net.mathias2246.buildmc.ui.UIUtil;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ProtectionUtil {
    public static @NotNull GuiItem createDisplayItem(@NotNull Player uiHolder, @NotNull Material material, String baseTranslationKey) {
        ItemStack displayBase = new ItemStack(material);
        ItemUtil.editMeta(displayBase, (meta) -> {
            meta.itemName(
                    Message.msg(uiHolder, baseTranslationKey+".name")
            );
            meta.addItemFlags(
                    ItemFlag.HIDE_ATTRIBUTES
            );

            // FIXME: Split component on newline characters
            meta.lore(List.of(Message.msg(uiHolder, baseTranslationKey + ".lore")));
        });
        return new GuiItem(
                displayBase,
                UIUtil.noInteract
        );
    }
}
