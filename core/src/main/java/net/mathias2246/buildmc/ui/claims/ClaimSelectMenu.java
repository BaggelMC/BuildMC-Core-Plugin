package net.mathias2246.buildmc.ui.claims;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.inventoryframework.adventuresupport.ComponentHolder;
import net.mathias2246.buildmc.inventoryframework.gui.GuiItem;
import net.mathias2246.buildmc.inventoryframework.gui.type.ChestGui;
import net.mathias2246.buildmc.inventoryframework.pane.OutlinePane;
import net.mathias2246.buildmc.inventoryframework.pane.PaginatedPane;
import net.mathias2246.buildmc.inventoryframework.pane.Pane;
import net.mathias2246.buildmc.inventoryframework.pane.StaticPane;
import net.mathias2246.buildmc.ui.UIUtil;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
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

        if (player.hasPermission("buildmc.admin")) {
            for (long id : ClaimManager.serverClaims) {
                Claim c = ClaimManager.getClaimByID(id);
                if (c != null) claims.add(c);
            }

            for (long id : ClaimManager.placeholderClaims) {
                Claim c = ClaimManager.getClaimByID(id);
                if (c != null) claims.add(c);
            }
        }

        ChestGui gui = new ChestGui(6, ComponentHolder.of(Message.msg(player, "messages.claims.ui.select-menu.title")));

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

        updatePageIndicator(player, controls, 0, Math.max(1, pages.getPages()), pages, gui);

        gui.addPane(controls);
        gui.show(player);
    }

    private static List<GuiItem> buildClaimItems(Player player, List<Claim> claims, Set<Long> playerClaimIds) {
        List<GuiItem> items = new ArrayList<>();

        for (Claim claim : claims) {
            Material material;
            switch (claim.getType()) {
                case PLAYER -> material = Material.PLAYER_HEAD;
                case TEAM -> material = Material.IRON_SWORD;
                case SERVER -> material = Material.COMMAND_BLOCK;
                case PLACEHOLDER -> material = Material.STRUCTURE_VOID;
                default -> material = Material.PAPER;
            }

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                CoreMain.plugin.sendMessage(player, Component.translatable("messages.claims.ui.errors.no-item-meta"));
                continue;
            }

            meta.addItemFlags(
                    ItemFlag.HIDE_ATTRIBUTES,
                    ItemFlag.HIDE_ADDITIONAL_TOOLTIP
            );

            meta.setDisplayName(LEGACY.serialize(Component.text(claim.getName(), NamedTextColor.GREEN)));

            List<String> lore = new ArrayList<>();
            String typeMessage;
            switch (claim.getType()) {
                case PLAYER -> typeMessage = LEGACY.serialize(Message.msg(player, "messages.claims.ui.select-menu.player-type"));
                case TEAM -> typeMessage = LEGACY.serialize(Message.msg(player, "messages.claims.ui.select-menu.team-type"));
                case SERVER -> typeMessage = LEGACY.serialize(Message.msg(player, "messages.claims.ui.select-menu.server-type"));
                case PLACEHOLDER -> typeMessage = LEGACY.serialize(Message.msg(player, "messages.claims.ui.select-menu.placeholder-type"));
                default -> typeMessage = LEGACY.serialize(Message.msg(player, "messages.claims.ui.select-menu.unknown-type"));
            }

            lore.add(typeMessage);

            lore.add(LEGACY.serialize(Message.msg(player,
                    "messages.claims.ui.select-menu.id",
                    Map.of("id", String.valueOf(claim.getId())))));

            meta.setLore(lore);
            item.setItemMeta(meta);

            items.add(new GuiItem(item, event -> {
                event.setCancelled(true);
                CoreMain.soundManager.playSound(player, UIUtil.CLICK_SOUND);
                ClaimEditMenu.open(player, claim);
            }));
        }

        return items;
    }

    private static void updatePageIndicator(Player player,
                                            StaticPane controls,
                                            int currentPage,
                                            int totalPages,
                                            PaginatedPane pages,
                                            ChestGui gui) {
        // Clear old navigation items
        controls.removeItem(2, 0);
        controls.removeItem(4, 0);
        controls.removeItem(6, 0);

        // --- Page indicator (middle) ---
        ItemStack indicator = makePageIndicator(player, currentPage + 1, totalPages);
        controls.addItem(new GuiItem(indicator, e -> e.setCancelled(true)), 4, 0);

        // --- Previous Button ---
        if (currentPage > 0) {
            ItemStack prev = createNavItem(player, "messages.claims.ui.general.previous");
            controls.addItem(new GuiItem(prev, e -> {
                e.setCancelled(true);
                CoreMain.soundManager.playSound(player, UIUtil.CLICK_SOUND);
                if (pages.getPage() > 0) {
                    pages.setPage(pages.getPage() - 1);
                    updatePageIndicator(player, controls, pages.getPage(), totalPages, pages, gui);
                    gui.update();
                }
            }), 2, 0);
        } else {
            // Replace with gray filler
            controls.addItem(new GuiItem(createDisabledNavItem(player), UIUtil.noInteract), 2, 0);
        }

        // --- Next Button ---
        if (currentPage < totalPages - 1) {
            ItemStack next = createNavItem(player, "messages.claims.ui.general.next");
            controls.addItem(new GuiItem(next, e -> {
                e.setCancelled(true);
                CoreMain.soundManager.playSound(player, UIUtil.CLICK_SOUND);
                if (pages.getPage() < pages.getPages() - 1) {
                    pages.setPage(pages.getPage() + 1);
                    updatePageIndicator(player, controls, pages.getPage(), totalPages, pages, gui);
                    gui.update();
                }
            }), 6, 0);
        } else {
            // Replace with gray filler
            controls.addItem(new GuiItem(createDisabledNavItem(player), UIUtil.noInteract), 6, 0);
        }
    }

    private static ItemStack createDisabledNavItem(Player player) {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setItemName(null);
            meta.setHideTooltip(true);
            item.setItemMeta(meta);
        }
        return item;
    }


    private static ItemStack makeInfoPaper(Player player, Component line) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            CoreMain.plugin.sendMessage(player, Component.translatable("messages.claims.ui.errors.no-item-meta"));
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
            CoreMain.plugin.sendMessage(player, Component.translatable("messages.claims.ui.errors.no-item-meta"));
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
            CoreMain.plugin.sendMessage(player, Component.translatable("messages.claims.ui.errors.no-item-meta"));
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
            CoreMain.plugin.sendMessage(player, Component.translatable("messages.claims.ui.errors.no-item-meta"));
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