package net.mathias2246.buildmc.claims.protections;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
            meta.setItemName(LegacyComponentSerializer.legacySection().serialize(
                    Message.msg(uiHolder, baseTranslationKey+".name")
            ));
            meta.addItemFlags(
                    ItemFlag.HIDE_ATTRIBUTES,
                    ItemFlag.HIDE_ADDITIONAL_TOOLTIP
            );
            meta.setLore(List.of(LegacyComponentSerializer.legacySection().serialize(Message.msg(uiHolder, baseTranslationKey + ".lore")).split("\n")));
        });
        return new GuiItem(
                displayBase,
                UIUtil.noInteract
        );
    }
}
