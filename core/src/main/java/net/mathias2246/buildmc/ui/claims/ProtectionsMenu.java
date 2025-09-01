package net.mathias2246.buildmc.ui.claims;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.ui.UIUtil;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

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
            GuiItem fillerItem = new GuiItem(createGlassPane(Material.LIGHT_GRAY_STAINED_GLASS_PANE), e -> e.setCancelled(true));
            for (int y = 0; y < 5; y++) {
                for (int x = 0; x < 9; x++) {
                    pane.addItem(fillerItem, x, y);
                }
            }

            int start = page * flagsPerPage;
            int end = Math.min(start + flagsPerPage, allFlags.size());
            int index = 0;

            for (int i = start; i < end; i++) {
                Protection protection = allFlags.get(i);
                if (protection.isHidden()) continue;
                boolean enabled = claim.hasProtection(protection.getKey());

                int x = index % 9;
                int y = (index < 9) ? 0 : 3; // First group or second group
                int statusRow = (index < 9) ? 1 : 4;

                pane.addItem(protection.getDisplay(player, gui), x, y);

                // Status glass
                ItemStack status = createStatusPane(protection.getKey(), enabled, player);
                GuiItem statusItem = new GuiItem(status, event -> {
                    event.setCancelled(true);

                    // Toggle the flag
                    toggleFlag(claim, protection.getKey(), enabled);

                    // Update the new status immediately
                    boolean newEnabled = claim.hasProtection(protection);
                    ItemStack newStatus = createStatusPane(protection.getKey(), newEnabled, player);

                    // Replace this slot with updated status item
                    pane.addItem(new GuiItem(newStatus, e2 -> {
                        e2.setCancelled(true);
                        // recursively handle further toggles
                        toggleFlag(claim, protection.getKey(), newEnabled);
                        // refresh this one slot again

                        // ItemStack nextStatus = createStatusPane(flag, nextEnabled, player);

                        pane.addItem(makeStatusItem(gui, pane, claim, protection, x, statusRow, player), x, statusRow);

                        gui.update();
                    }), x, statusRow);

                    // Refresh UI without reopening
                    gui.update();
                });

                pane.addItem(statusItem, x, statusRow);

                index++;
            }

            // Spacer
            for (int x = 0; x < 9; x++) {
                pane.addItem(new GuiItem(createGlassPane(Material.GRAY_STAINED_GLASS_PANE), UIUtil.noInteract), x, 2);
            }

            pages.addPane(page, pane);
        }

        pages.setPage(0);
        gui.addPane(pages);

        // Nav bar
        StaticPane controls = new StaticPane(0, 5, 9, 1);
        GuiItem grayItem = new GuiItem(createGlassPane(Material.GRAY_STAINED_GLASS_PANE), e -> e.setCancelled(true));
        for (int i = 0; i < 9; i++) {
            controls.addItem(grayItem, i, 0);
        }

        // Prev button
        controls.addItem(new GuiItem(createNamedItem(Material.ARROW, Message.msg(player,"messages.claims.ui.general.previous")), e -> {
            e.setCancelled(true);
            if (pages.getPage() > 0) {
                int newPage = pages.getPage() - 1;
                pages.setPage(newPage);
                updatePageIndicator(player, controls, newPage + 1, totalPages);
                gui.update();
            }
        }), 2, 0);

        // Page indicator
        updatePageIndicator(player, controls, pages.getPage() + 1, totalPages);

        // Next button
        controls.addItem(new GuiItem(createNamedItem(Material.ARROW, Message.msg(player,"messages.claims.ui.general.next")), e -> {
            e.setCancelled(true);
            if (pages.getPage() < totalPages - 1) {
                int newPage = pages.getPage() + 1;
                pages.setPage(newPage);
                updatePageIndicator(player, controls, newPage + 1, totalPages);
                gui.update();
            }
        }), 6, 0);

        // Back button
        controls.addItem(new GuiItem(createNamedItem(Material.BARRIER, Message.msg(player,"messages.claims.ui.general.back")), e -> {
            e.setCancelled(true);
            ClaimEditMenu.open(player, claim); // Navigate back to Claim Edit Menu
        }), 8, 0);

        gui.addPane(controls);
        gui.show(player);
    }

    private static GuiItem makeStatusItem(ChestGui gui, StaticPane pane,
                                          Claim claim, Protection protection,
                                          int x, int y, @NotNull Player player) {
        boolean enabled = claim.hasProtection(protection);
        ItemStack status = createStatusPane(protection.getKey(), enabled, player);

        return new GuiItem(status, e -> {
            e.setCancelled(true);

            // Toggle
            if (enabled) {
                ClaimManager.removeProtection(claim, protection);
            } else {
                ClaimManager.addProtection(claim, protection);
            }

            // Replace this slot with a freshly built GuiItem (so it keeps toggling)
            GuiItem updated = makeStatusItem(gui, pane, claim, protection, x, y, player);
            pane.addItem(updated, x, y);

            gui.update();
        });
    }


    private static void toggleFlag(@NotNull Claim claim, @NotNull NamespacedKey protection, boolean currentlyEnabled) {
        if (currentlyEnabled) {
            ClaimManager.removeProtection(claim, protection);
        } else {
            ClaimManager.addProtection(claim, protection);
        }
    }


    private static ItemStack createStatusPane(NamespacedKey protection, boolean enabled, @NotNull Player player) {
        Material color = enabled ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        ItemStack item = new ItemStack(color);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(Message.msg(player, enabled ? "messages.claims.ui.protections-menu.enabled" : "messages.claims.ui.protections-menu.disabled")));
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createGlassPane(Material material) {
        ItemStack pane = new ItemStack(material);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.setHideTooltip(true);
            pane.setItemMeta(meta);
        }
        return pane;
    }

    @SuppressWarnings("SameParameterValue")
    private static ItemStack createNamedItem(Material material, Component name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    private static void updatePageIndicator(Player player, StaticPane controls, int current, int total) {
        controls.removeItem(4, 0);
        ItemStack pageIndicator = new ItemStack(Material.PAPER);
        ItemMeta meta = pageIndicator.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(Message.msg(player, "messages.claims.ui.general.page-indicator", Map.of("current", String.valueOf(current), "total", String.valueOf(total)))));
            pageIndicator.setItemMeta(meta);
        }
        controls.addItem(new GuiItem(pageIndicator, e -> e.setCancelled(true)), 4, 0);
    }

}
