package net.mathias2246.buildmc.claims.protections.entities;

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
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class HangingEntities extends Protection {

    public HangingEntities(@Nullable ConfigurationSection section) {
        super(Objects.requireNonNull(NamespacedKey.fromString("buildmc:hanging_entities")), (section != null ? section.getBoolean("default", true) : true), section != null && section.getBoolean("is-hidden", false));
    }

    @Override
    public @NotNull GuiItem getDisplay(@NotNull Player uiHolder, @NotNull Gui gui) {

        String t = getTranslationBaseKey();

        ItemStack displayBase = new ItemStack(Material.PAINTING);
        ItemUtil.editMeta(displayBase, (meta) -> {
            meta.setItemName(LegacyComponentSerializer.legacySection().serialize(
                    Message.msg(uiHolder, t+".name")
            ));
            meta.setLore(List.of(LegacyComponentSerializer.legacySection().serialize(Message.msg(uiHolder, t + ".lore")).split("\n")));
        });

        return new GuiItem(
                displayBase,
                UIUtil.noInteract
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPaintingBreak(HangingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player player)) return;

        if (!ClaimManager.isPlayerAllowed(player, getKey(), event.getEntity().getLocation())) {
            event.setCancelled(true);
            CoreMain.plugin.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.entity-damage"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPaintingPlace(HangingPlaceEvent event) {
        Player player = event.getPlayer();

        if (player == null) return;

        if (!ClaimManager.isPlayerAllowed(player, getKey(), event.getEntity().getLocation())) {
            event.setCancelled(true);
            CoreMain.plugin.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.block-place"));
        }
    }

    @Override
    public String getTranslationBaseKey() {
        return "claims.flags.hanging-entities";
    }
}
