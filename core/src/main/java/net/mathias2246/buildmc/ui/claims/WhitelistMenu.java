package net.mathias2246.buildmc.ui.claims;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.claims.Claim;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WhitelistMenu {

    public static void open(Player player, Claim claim) {
        List<UUID> whitelist = new ArrayList<>(claim.getWhitelistedPlayers());
        int playersPerPage = 18; // Two rows of heads per page
        int totalPages = Math.max(1, (int) Math.ceil((double) whitelist.size() / playersPerPage));

        ChestGui gui = new ChestGui(6, ComponentHolder.of(
                Message.msg(player, "messages.claims.ui.whitelist-menu.title", Map.of("claim", claim.getName()))
        ));
        PaginatedPane pages = new PaginatedPane(0, 0, 9, 5);

        for (int page = 0; page < totalPages; page++) {
            StaticPane pane = new StaticPane(0, 0, 9, 5);

            // Fill background
            GuiItem filler = new GuiItem(createGlassPane(Material.LIGHT_GRAY_STAINED_GLASS_PANE), e -> e.setCancelled(true));
            for (int x = 0; x < 9; x++) {
                for (int y = 0; y < 5; y++) {
                    pane.addItem(filler, x, y);
                }
            }

            // Separator row (row 3)
            for (int x = 0; x < 9; x++) {
                pane.addItem(new GuiItem(createGlassPane(Material.GRAY_STAINED_GLASS_PANE), e -> e.setCancelled(true)), x, 2);
            }

            int start = page * playersPerPage;
            int end = Math.min(start + playersPerPage, whitelist.size());

            int index = 0;
            for (int i = start; i < end; i++) {
                UUID uuid = whitelist.get(i);
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                String playerName = offlinePlayer.getName() != null ? offlinePlayer.getName() : "Unknown";

                // Head position
                int headX = index % 9;
                int headY = (index < 9) ? 0 : 3;

                // Delete button position
                int deleteY = (index < 9) ? 1 : 4;

                // Player Head
                ItemStack head = createPlayerHead(offlinePlayer, playerName);
                pane.addItem(new GuiItem(head, e -> e.setCancelled(true)), headX, headY);

                // Delete button
                ItemStack delete = createDeleteButton(player);
                pane.addItem(new GuiItem(delete, e -> {
                    e.setCancelled(true);
                    openDeleteConfirmationMenu(player, claim, uuid, offlinePlayer.getName());
                }), headX, deleteY);

                index++;
            }

            pages.addPane(page, pane);
        }

        pages.setPage(0);
        gui.addPane(pages);

        // Navigation bar
        StaticPane controls = new StaticPane(0, 5, 9, 1);
        GuiItem grayItem = new GuiItem(createGlassPane(Material.GRAY_STAINED_GLASS_PANE), e -> e.setCancelled(true));
        for (int i = 0; i < 9; i++) {
            controls.addItem(grayItem, i, 0);
        }

        // Add button
//        controls.addItem(new GuiItem(createNamedItem(Material.EMERALD, Component.translatable("messages.claims.ui.whitelist-menu.add")), e -> {
//            e.setCancelled(true);
//            CoreMain.mainClass.sendPlayerMessage(player, Component.text("Add player to whitelist (not implemented yet)", NamedTextColor.GRAY));
//        }), 0, 0);

        // Back button
        controls.addItem(new GuiItem(createNamedItem(Material.BARRIER, Message.msg(player,"messages.claims.ui.general.back")), e -> {
            e.setCancelled(true);
            ClaimEditMenu.open(player, claim); // Navigate back to Claim Edit Menu
        }), 8, 0);

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
        controls.addItem(new GuiItem(createNamedItem(Material.ARROW, Message.msg(player, "messages.claims.ui.general.next")), e -> {
            e.setCancelled(true);
            if (pages.getPage() < totalPages - 1) {
                int newPage = pages.getPage() + 1;
                pages.setPage(newPage);
                updatePageIndicator(player, controls, newPage + 1, totalPages);
                gui.update();
            }
        }), 6, 0);

        gui.addPane(controls);
        gui.show(player);
    }

    private static void openDeleteConfirmationMenu(Player player, Claim claim, UUID uuid, String playerName) {
        ChestGui gui = new ChestGui(3,
                ComponentHolder.of(Message.msg(player, "messages.claims.ui.whitelist-menu.delete-confirm-menu.title", Map.of("player", playerName))));

        StaticPane pane = new StaticPane(0, 0, 9, 3);

        // Fill with red glass
        GuiItem filler = new GuiItem(createGlassPane(Material.RED_STAINED_GLASS_PANE), e -> e.setCancelled(true));
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 3; y++) {
                pane.addItem(filler, x, y);
            }
        }

        // Cancel button
        pane.addItem(new GuiItem(createNamedItem(Material.GREEN_CONCRETE, Message.msg(player,"messages.claims.ui.general.cancel")), e -> {
            e.setCancelled(true);
            open(player, claim); // reopen whitelist menu
        }), 3, 1);

        // Confirm button
        pane.addItem(new GuiItem(createNamedItem(Material.RED_CONCRETE, Message.msg(player,"messages.claims.ui.general.confirm")), e -> {
            e.setCancelled(true);
            ClaimManager.removePlayerFromWhitelist(claim.getId(), uuid);
            CoreMain.mainClass.sendPlayerMessage(player, Component.translatable("messages.claims.ui.whitelist-menu.delete-confirm-menu.success"));
            open(player, claim);
        }), 5, 1);

        gui.addPane(pane);
        gui.show(player);
    }

    private static ItemStack createPlayerHead(OfflinePlayer offlinePlayer, String name) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(offlinePlayer);
            meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(Component.text(name, NamedTextColor.WHITE)));
            head.setItemMeta(meta);
        }
        return head;
    }

    private static ItemStack createDeleteButton(@NotNull Player player) {
        ItemStack item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(Message.msg(player, "messages.claims.ui.whitelist-menu.remove")));
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
            meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(
                    Message.msg(player, "messages.claims.ui.general.page-indicator", Map.of("current", String.valueOf(current), "total", String.valueOf(total)))
            ));
            pageIndicator.setItemMeta(meta);
        }
        controls.addItem(new GuiItem(pageIndicator, e -> e.setCancelled(true)), 4, 0);
    }
}
