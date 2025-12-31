package net.mathias2246.buildmc.ui.claims;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.claims.ClaimLogger;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.inventoryframework.adventuresupport.ComponentHolder;
import net.mathias2246.buildmc.inventoryframework.gui.GuiItem;
import net.mathias2246.buildmc.inventoryframework.gui.type.ChestGui;
import net.mathias2246.buildmc.inventoryframework.pane.PaginatedPane;
import net.mathias2246.buildmc.inventoryframework.pane.StaticPane;
import net.mathias2246.buildmc.ui.UIUtil;
import net.mathias2246.buildmc.util.Message;
import net.mathias2246.buildmc.util.SoundUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class ProtectionsMenu {

    public static void open(Player player, Claim claim) {
        List<Protection> allFlags = CoreMain.protectionsRegistry.stream().toList();
        int flagsPerPage = 18; // Two sets of 9 per page
        int totalPages = Math.max(1, (int) Math.ceil((double) allFlags.size() / flagsPerPage));

        ChestGui gui = new ChestGui(6, ComponentHolder.of(Message.msg(player, "messages.claims.ui.protections-menu.title", Map.of("claim", claim.getName()))));
        PaginatedPane pages = new PaginatedPane(0, 0, 9, 5);

        for (int page = 0; page < totalPages; page++) {
            StaticPane pane = new StaticPane(0, 0, 9, 5);

            // Fill background
            for (int y = 0; y < 5; y++) {
                for (int x = 0; x < 9; x++) {
                    pane.addItem(UIUtil.INVISIBLE_PANE, x, y);
                }
            }

            int start = page * flagsPerPage;
            int end = Math.min(start + flagsPerPage, allFlags.size());
            int index = 0;

            for (int i = start; i < end; i++) {
                Protection protection = allFlags.get(i);
                if (protection.isHidden()) continue;

                int x = index % 9;
                int y = (index < 9) ? 0 : 3; // First group or second group
                int statusRow = (index < 9) ? 1 : 4;

                // Display item
                pane.addItem(protection.getDisplay(player, gui), x, y);

                GuiItem statusItem = makeStatusItem(gui, pane, claim, protection, x, statusRow, player);
                if (statusItem != null) pane.addItem(statusItem, x, statusRow);

                index++;
            }

            // Spacer row
            for (int x = 0; x < 9; x++) {
                pane.addItem(new GuiItem(createGlassPane(), UIUtil.noInteract), x, 2);
            }

            pages.addPane(page, pane);
        }

        pages.setPage(0);
        gui.addPane(pages);

        // Nav bar
        StaticPane controls = new StaticPane(0, 5, 9, 1);
        GuiItem grayItem = new GuiItem(createGlassPane(), e -> e.setCancelled(true));
        for (int i = 0; i < 9; i++) {
            controls.addItem(grayItem, i, 0);
        }

        updatePageIndicator(player, controls, pages.getPage() + 1, totalPages, pages, gui);

        // Back button
        controls.addItem(new GuiItem(createNamedItem(Material.BARRIER, Message.msg(player,"messages.claims.ui.general.back")), e -> {
            e.setCancelled(true);
            CoreMain.soundManager.playSound(player, SoundUtil.uiClick);
            ClaimEditMenu.open(player, claim);
        }), 8, 0);

        gui.addPane(controls);
        gui.show(player);
    }

    @Nullable
    private static GuiItem makeStatusItem(ChestGui gui, StaticPane pane,
                                          Claim claim, Protection protection,
                                          int x, int y, @NotNull Player player) {

        boolean enabled = claim.hasProtection(protection);
        ItemStack status = createStatusPane(protection.getKey(), enabled, player);

        return new GuiItem(status, event -> {
            event.setCancelled(true);
            CoreMain.soundManager.playSound(player, SoundUtil.uiClick);

            boolean currentlyEnabled = claim.hasProtection(protection);

            // Update claim + log
            if (currentlyEnabled) {
                ClaimManager.removeProtection(claim, protection);
                ClaimLogger.logProtectionChanged(player, claim.getName(), protection, "disabled");
            } else {
                ClaimManager.addProtection(claim, protection);
                ClaimLogger.logProtectionChanged(player, claim.getName(), protection, "enabled");
            }

            // Refresh status pane
            GuiItem updated = makeStatusItem(gui, pane, claim, protection, x, y, player);
            if (updated != null) {
                pane.addItem(updated, x, y);
                gui.update();
            }
        });
    }

    private static ItemStack createStatusPane(NamespacedKey protection, boolean enabled, @NotNull Player player) {
        Material color = enabled ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        ItemStack item = new ItemStack(color);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(
                    Message.msg(player, enabled ? "messages.claims.ui.protections-menu.enabled" : "messages.claims.ui.protections-menu.disabled")
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createGlassPane() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.setHideTooltip(true);
            pane.setItemMeta(meta);
        }
        return pane;
    }

    private static ItemStack createNamedItem(Material material, Component name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    private static void updatePageIndicator(Player player,
                                            StaticPane controls,
                                            int current,
                                            int total,
                                            PaginatedPane pages,
                                            ChestGui gui) {

        controls.removeItem(2, 0);
        controls.removeItem(4, 0);
        controls.removeItem(6, 0);

        // Page indicator
        ItemStack pageIndicator = new ItemStack(Material.PAPER);
        ItemMeta meta = pageIndicator.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(
                    Message.msg(player, "messages.claims.ui.general.page-indicator",
                            Map.of("current", String.valueOf(current), "total", String.valueOf(total)))
            ));
            pageIndicator.setItemMeta(meta);
        }
        controls.addItem(new GuiItem(pageIndicator, e -> e.setCancelled(true)), 4, 0);

        // Previous button
        if (current > 1) {
            controls.addItem(new GuiItem(
                    createNamedItem(Material.ARROW, Message.msg(player, "messages.claims.ui.general.previous")),
                    e -> {
                        e.setCancelled(true);
                        CoreMain.soundManager.playSound(player, SoundUtil.uiClick);
                        int newPage = pages.getPage() - 1;
                        pages.setPage(newPage);
                        updatePageIndicator(player, controls, newPage + 1, total, pages, gui);
                        gui.update();
                    }), 2, 0);
        } else {
            controls.addItem(new GuiItem(createGlassPane(), UIUtil.noInteract), 2, 0);
        }

        // Next button
        if (current < total) {
            controls.addItem(new GuiItem(
                    createNamedItem(Material.ARROW, Message.msg(player, "messages.claims.ui.general.next")),
                    e -> {
                        e.setCancelled(true);
                        CoreMain.soundManager.playSound(player, SoundUtil.uiClick);
                        int newPage = pages.getPage() + 1;
                        pages.setPage(newPage);
                        updatePageIndicator(player, controls, newPage + 1, total, pages, gui);
                        gui.update();
                    }), 6, 0);
        } else {
            controls.addItem(new GuiItem(createGlassPane(), UIUtil.noInteract), 6, 0);
        }
    }

}
