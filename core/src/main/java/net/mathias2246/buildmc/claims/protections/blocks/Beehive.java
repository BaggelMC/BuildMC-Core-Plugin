package net.mathias2246.buildmc.claims.protections.blocks;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.api.item.ItemUtil;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.inventoryframework.gui.GuiItem;
import net.mathias2246.buildmc.inventoryframework.gui.type.util.Gui;
import net.mathias2246.buildmc.ui.UIUtil;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class Beehive extends Protection {

    @SuppressWarnings("SimplifiableConditionalExpression")
    public Beehive(@Nullable ConfigurationSection section) {
        super(Objects.requireNonNull(NamespacedKey.fromString("buildmc:beehive_interactions")), (section != null ? section.getBoolean("default", true) : true), section != null && section.getBoolean("is-hidden", false));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBeehiveInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        Material type = event.getClickedBlock().getType();
        if (type != Material.BEEHIVE && type != Material.BEE_NEST) return;

        Player player = event.getPlayer();
        if (!ClaimManager.isPlayerAllowed(player, getKey(), event.getClickedBlock().getLocation())) {
            event.setCancelled(true);
            CoreMain.plugin.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.interact"));
        }
    }

    @Override
    public String getTranslationBaseKey() {
        return "claims.flags.interaction-beehives";
    }

    @Override
    public @NotNull GuiItem getDisplay(@NotNull Player uiHolder, @NotNull Gui gui) {
        String t = getTranslationBaseKey();

        ItemStack displayBase = new ItemStack(Material.BEEHIVE);
        ItemUtil.editMeta(displayBase, (meta) -> {
            meta.setItemName(LegacyComponentSerializer.legacySection().serialize(
                    Message.msg(uiHolder, t+".name")
            ));
            meta.addItemFlags(
                    ItemFlag.HIDE_ATTRIBUTES,
                    ItemFlag.HIDE_ADDITIONAL_TOOLTIP
            );

            meta.setLore(List.of(LegacyComponentSerializer.legacySection().serialize(Message.msg(uiHolder, t + ".lore")).split("\n")));
        });

        return new GuiItem(
                displayBase,
                UIUtil.noInteract
        );
    }
}
