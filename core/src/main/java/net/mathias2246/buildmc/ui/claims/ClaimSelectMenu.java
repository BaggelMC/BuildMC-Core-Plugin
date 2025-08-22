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
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ClaimSelectMenu {

    public static void open(Player player) {
        List<Claim> claims = new ArrayList<>();

        // Player claims
        List<Long> playerClaims = ClaimManager.playerOwner.getOrDefault(player.getUniqueId(), Collections.emptyList());
        for (long id : playerClaims) {
            Claim claim = ClaimManager.getClaimByID(id);
            if (claim != null) claims.add(claim);
        }

        // Team claims
        var team = ClaimManager.getPlayerTeam(player);
        if (team != null) {
            List<Long> teamClaims = ClaimManager.teamOwner.getOrDefault(team.getName(), Collections.emptyList());
            for (long id : teamClaims) {
                Claim claim = ClaimManager.getClaimByID(id);
                if (claim != null) claims.add(claim);
            }
        }

        ChestGui gui = new ChestGui(6, ComponentHolder.of(Component.translatable("messages.claims.ui.select-menu.title")));
        PaginatedPane pages = new PaginatedPane(0, 0, 9, 5);

        // Claim items
        List<GuiItem> items = new ArrayList<>();
        for (Claim claim : claims) {
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();

            if (meta == null) {
                CoreMain.mainClass.sendPlayerMessage(player, Component.translatable("messages.claims.ui.errors.no-item-meta"));
                return;
            }

            meta.setDisplayName(LegacyComponentSerializer.legacySection()
                    .serialize(Component.text(claim.getName(), NamedTextColor.GREEN)));

            List<String> lore = new ArrayList<>();
            if (playerClaims.contains(claim.getId())) {
                lore.add(LegacyComponentSerializer.legacySection()
                        .serialize(Message.msg(player, "messages.claims.ui.select-menu.player-type")));
            } else {
                lore.add(LegacyComponentSerializer.legacySection()
                        .serialize(Message.msg(player, "messages.claims.ui.select-menu.team-type")));
            }
            lore.add(LegacyComponentSerializer.legacySection()
                    .serialize(Message.msg(player, "messages.claims.ui.select-menu.id", Map.of("id", String.valueOf(claim.getId())))));
            meta.setLore(lore);

            item.setItemMeta(meta);

            GuiItem guiItem = new GuiItem(item, event -> {
                event.setCancelled(true);
                ClaimEditMenu.open(player, claim);
            });

            items.add(guiItem);
        }

        // Populate pages
        int itemsPerPage = 45; // first 5 rows
        int totalPages = Math.max(1, (int) Math.ceil((double) items.size() / itemsPerPage));

        for (int page = 0; page < totalPages; page++) {
            StaticPane pane = new StaticPane(0, 0, 9, 5);

            // Filler
            for (int y = 0; y < 5; y++) {
                for (int x = 0; x < 9; x++) {
                    ItemStack filler = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
                    ItemMeta fillerMeta = filler.getItemMeta();
                    if (fillerMeta == null) {
                        CoreMain.mainClass.sendPlayerMessage(player, Component.translatable("messages.claims.ui.errors.no-item-meta"));
                        return;
                    }
                    fillerMeta.setHideTooltip(true);
                    filler.setItemMeta(fillerMeta);
                    pane.addItem(new GuiItem(filler, e -> e.setCancelled(true)), x, y);
                }
            }

            int start = page * itemsPerPage;
            int end = Math.min(start + itemsPerPage, items.size());
            List<GuiItem> subList = items.subList(start, end);

            int index = 0;
            for (GuiItem guiItem : subList) {
                int x = index % 9;
                int y = index / 9;
                pane.addItem(guiItem, x, y);
                index++;
            }

            pages.addPane(page, pane);
        }

        gui.addPane(pages);

        // Nav bar
        StaticPane controls = new StaticPane(0, 5, 9, 1);

        ItemStack grayPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta grayMeta = grayPane.getItemMeta();
        if (grayMeta == null) {
            CoreMain.mainClass.sendPlayerMessage(player, Component.translatable("messages.claims.ui.errors.no-item-meta"));
            return;
        }
        grayMeta.setHideTooltip(true);
        grayPane.setItemMeta(grayMeta);

        for (int i = 0; i < 9; i++) {
            controls.addItem(new GuiItem(grayPane, e -> e.setCancelled(true)), i, 0);
        }

        // Prev button
        ItemStack prevItem = new ItemStack(Material.ARROW);
        ItemMeta prevMeta = prevItem.getItemMeta();
        if (prevMeta == null) {
            CoreMain.mainClass.sendPlayerMessage(player, Component.translatable("messages.claims.ui.errors.no-item-meta"));
            return;
        }
        prevMeta.setDisplayName(LegacyComponentSerializer.legacySection()
                .serialize(Message.msg(player, "messages.claims.ui.general.previous")));
        prevItem.setItemMeta(prevMeta);

        controls.addItem(new GuiItem(prevItem, event -> {
            event.setCancelled(true);
            if (pages.getPage() > 0) {
                pages.setPage(pages.getPage() - 1);
                gui.update();
            }
        }), 2, 0);

        // Page indicator
        controls.addItem(new GuiItem(makePageIndicator(player, 1, totalPages), e -> e.setCancelled(true)), 4, 0);

        // Next button
        ItemStack nextItem = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = nextItem.getItemMeta();
        if (nextMeta == null) {
            CoreMain.mainClass.sendPlayerMessage(player, Component.translatable("messages.claims.ui.errors.no-item-meta"));
            return;
        }
        nextMeta.setDisplayName(LegacyComponentSerializer.legacySection()
                .serialize(Message.msg(player, "messages.claims.ui.general.next")));
        nextItem.setItemMeta(nextMeta);

        controls.addItem(new GuiItem(nextItem, event -> {
            event.setCancelled(true);
            if (pages.getPage() < totalPages - 1) {
                pages.setPage(pages.getPage() + 1);
                gui.update();
            }
        }), 6, 0);

        gui.addPane(controls);

        // Update page indicator
        gui.setOnGlobalClick(event -> {
            controls.removeItem(4, 0);
            controls.addItem(new GuiItem(makePageIndicator(player, pages.getPage() + 1, totalPages), e -> e.setCancelled(true)), 4, 0);
            gui.update();
        });

        gui.show(player);
    }

    private static ItemStack makePageIndicator(Player player, int current, int total) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            CoreMain.mainClass.sendPlayerMessage(player, Component.translatable("messages.claims.ui.errors.no-item-meta"));
            return new ItemStack(Material.PAPER);
        }
        meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(Message.msg(player, "messages.claims.ui.general.page-indicator", Map.of("current", String.valueOf(current), "total", String.valueOf(total)))));
        item.setItemMeta(meta);
        return item;
    }
}
