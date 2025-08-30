package net.mathias2246.buildmc.ui.claims;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.ui.UIUtil;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ClaimSelectMenu {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    public static void open(Player player) {
        // Get claims
        List<Long> playerClaimIds = ClaimManager.playerOwner
                .getOrDefault(player.getUniqueId(), Collections.emptyList());
        Set<Long> playerClaimIdSet = new HashSet<>(playerClaimIds); // quick lookup for lore

        List<Long> teamClaimIds = Optional.ofNullable(ClaimManager.getPlayerTeam(player))
                .map(t -> ClaimManager.teamOwner.getOrDefault(t.getName(), Collections.emptyList()))
                .orElse(Collections.emptyList());

        List<Claim> claims = new ArrayList<>();
        for (long id : playerClaimIds) {
            Claim c = ClaimManager.getClaimByID(id);
            if (c != null) claims.add(c);
        }
        for (long id : teamClaimIds) {
            Claim c = ClaimManager.getClaimByID(id);
            if (c != null) claims.add(c);
        }

        ChestGui gui = new ChestGui(6, ComponentHolder.of(Component.translatable("messages.claims.ui.select-menu.title")));

        StaticPane background = new StaticPane(0, 0, 9, 5);
        background.setPriority(Pane.Priority.LOW);
        ItemStack filler = createFiller(player);
        background.fillWith(filler, e -> e.setCancelled(true));
        gui.addPane(background);

        PaginatedPane pages = new PaginatedPane(0, 0, 9, 5);
        List<GuiItem> items = buildClaimItems(player, claims, playerClaimIdSet);

        if (items.isEmpty()) {

            OutlinePane placeholder = new OutlinePane(0, 0, 9, 5);
            placeholder.addItem(new GuiItem(makeInfoPaper(player,
                    Message.msg(player, "messages.claims.ui.select-menu.empty")), e -> e.setCancelled(true)));
            pages.addPane(0, placeholder);
        } else {
            final int itemsPerPage = 45; // 9 * 5
            int totalPages = (int) Math.ceil(items.size() / (double) itemsPerPage);

            for (int page = 0; page < totalPages; page++) {
                int start = page * itemsPerPage;
                int end = Math.min(start + itemsPerPage, items.size());

                OutlinePane pagePane = new OutlinePane(0, 0, 9, 5);
                pagePane.setPriority(Pane.Priority.NORMAL);
                items.subList(start, end).forEach(pagePane::addItem);

                pages.addPane(page, pagePane);
            }
        }

        gui.addPane(pages);

        StaticPane controls = UIUtil.BOTTOM_BAR.copy();
        controls.setPriority(Pane.Priority.HIGHEST);

        // Prev button
        ItemStack prevItem = createNavItem(player, "messages.claims.ui.general.previous");
        controls.addItem(new GuiItem(prevItem, event -> {
            event.setCancelled(true);
            if (pages.getPage() > 0) {
                pages.setPage(pages.getPage() - 1);
                updatePageIndicator(player, controls, pages.getPage(), Math.max(1, pages.getPages()));
                gui.update();
            }
        }), 2, 0);

        // Page indicator
        updatePageIndicator(player, controls, 0, Math.max(1, pages.getPages()));

        // Next button
        ItemStack nextItem = createNavItem(player, "messages.claims.ui.general.next");
        controls.addItem(new GuiItem(nextItem, event -> {
            event.setCancelled(true);
            if (pages.getPage() < pages.getPages() - 1) {
                pages.setPage(pages.getPage() + 1);
                updatePageIndicator(player, controls, pages.getPage(), Math.max(1, pages.getPages()));
                gui.update();
            }
        }), 6, 0);

        gui.addPane(controls);
        gui.show(player);
    }

    private static List<GuiItem> buildClaimItems(Player player, List<Claim> claims, Set<Long> playerClaimIds) {
        List<GuiItem> items = new ArrayList<>();

        for (Claim claim : claims) {
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                CoreMain.mainClass.sendPlayerMessage(player, Component.translatable("messages.claims.ui.errors.no-item-meta"));
                continue;
            }

            meta.setDisplayName(LEGACY.serialize(Component.text(claim.getName(), NamedTextColor.GREEN)));

            List<String> lore = new ArrayList<>();
            lore.add(LEGACY.serialize(playerClaimIds.contains(claim.getId())
                    ? Message.msg(player, "messages.claims.ui.select-menu.player-type")
                    : Message.msg(player, "messages.claims.ui.select-menu.team-type")));

            lore.add(LEGACY.serialize(Message.msg(player,
                    "messages.claims.ui.select-menu.id",
                    Map.of("id", String.valueOf(claim.getId())))));

            meta.setLore(lore);
            item.setItemMeta(meta);

            items.add(new GuiItem(item, event -> {
                event.setCancelled(true);
                ClaimEditMenu.open(player, claim);
            }));
        }

        return items;
    }

    private static void updatePageIndicator(Player player, StaticPane controls, int currentPage, int totalPages) {
        controls.removeItem(4, 0);
        controls.addItem(new GuiItem(makePageIndicator(player, currentPage + 1, totalPages), e -> e.setCancelled(true)), 4, 0);
    }

    private static ItemStack makeInfoPaper(Player player, Component line) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            CoreMain.mainClass.sendPlayerMessage(player, Component.translatable("messages.claims.ui.errors.no-item-meta"));
            return item;
        }
        meta.setDisplayName(LEGACY.serialize(line));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createFiller(Player player) {
        ItemStack filler = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        if (meta == null) {
            CoreMain.mainClass.sendPlayerMessage(player, Component.translatable("messages.claims.ui.errors.no-item-meta"));
            return filler;
        }
        meta.setHideTooltip(true);
        filler.setItemMeta(meta);
        return filler;
    }

    private static ItemStack createNavItem(Player player, String messageKey) {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            CoreMain.mainClass.sendPlayerMessage(player, Component.translatable("messages.claims.ui.errors.no-item-meta"));
            return item;
        }
        meta.setDisplayName(LEGACY.serialize(Message.msg(player, messageKey)));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack makePageIndicator(Player player, int current, int total) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            CoreMain.mainClass.sendPlayerMessage(player, Component.translatable("messages.claims.ui.errors.no-item-meta"));
            return item;
        }
        meta.setDisplayName(LEGACY.serialize(
                Message.msg(player,
                        "messages.claims.ui.general.page-indicator",
                        Map.of("current", String.valueOf(current), "total", String.valueOf(total))
                )
        ));
        item.setItemMeta(meta);
        return item;
    }
}