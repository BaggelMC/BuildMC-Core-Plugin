package net.mathias2246.buildmc.claims.protections;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.api.item.ItemUtil;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.ui.UIUtil;
import net.mathias2246.buildmc.util.AudienceUtil;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public static <T extends Cancellable> void handleProtection(
            @NotNull T event,
            @NotNull Protection protection,
            @NotNull Location at,
            @NotNull Player player
    )
    {
        Claim claim = ClaimManager.getClaim(at);
        if (claim == null) return;

        if (!ClaimManager.isPlayerAllowed(player, protection.getKey(), claim)) {
            event.setCancelled(true);
            AudienceUtil.sendActionBar(player, Component.translatable(protection.getTranslationBaseKey()+".message"));
        }
    }

    /** Small method for checking if the given {@link PlayerInteractEvent} right-clicked a block or not.
     * <p>
     *     Otherwise returns {@code null}.
     * </p>
     * **/
    public static @Nullable Block rightClickBlock(PlayerInteractEvent event) {
        Action actionUsed = event.getAction();

        // Check if it's a relevant action
        if (actionUsed != Action.RIGHT_CLICK_BLOCK) return null;
        return event.getClickedBlock();
    }
}
