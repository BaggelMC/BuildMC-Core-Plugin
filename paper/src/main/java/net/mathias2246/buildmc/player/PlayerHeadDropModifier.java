package net.mathias2246.buildmc.player;

import net.mathias2246.buildmc.api.item.ItemMetaModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlayerHeadDropModifier implements ItemMetaModifier {
    @Override
    public void modifyMeta(@NotNull ItemMeta meta, @Nullable Player player) {

    }

    @Override
    public void modifyMeta(@NotNull ItemMeta meta, @Nullable Player player, @Nullable Object arg) {
        if (!(arg instanceof PlayerDeathEvent event) || player == null) return;
        var msg = event.deathMessage();
        if (msg == null) return;
        meta.lore(
                List.of(
                        msg
                )
        );

    }
}
