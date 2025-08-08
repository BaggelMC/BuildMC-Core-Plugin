package net.mathias2246.buildmc.claims;

import net.mathias2246.buildmc.item.ItemMetaModifier;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ClaimToolItemMetaModifier implements ItemMetaModifier {
    @Override
    public void modifyMeta(@NotNull ItemMeta meta, @Nullable Player player) {
        meta.itemName(Message.msg(player, "messages.claims.tool.tool-name"));
        meta.lore(
                List.of(
                        Message.msg(player, "messages.claims.tool.tool-tooltip-line1"),
                        Message.msg(player, "messages.claims.tool.tool-tooltip-line2"),
                        Message.msg(player, "messages.claims.tool.tool-tooltip-line3"),
                        Message.msg(player, "messages.claims.tool.tool-tooltip-line4")
                )
        );
    }

    @Override
    public void modifyMeta(@NotNull ItemMeta meta, @Nullable Player player, @Nullable Object object) {

    }
}
