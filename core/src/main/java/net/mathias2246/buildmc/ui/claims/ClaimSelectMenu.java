package net.mathias2246.buildmc.ui.claims;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.claims.Claim;
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
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

import static net.mathias2246.buildmc.CoreMain.plugin;
import static net.mathias2246.buildmc.ui.UIUtil.LEGACY_COMPONENT_SERIALIZER;

public class ClaimSelectMenu {

    public static final int SLOTS_PER_PAGE = 45;

    public static void open(Player player) {
        // Get claims
        List<Long> playerClaimIds = ClaimManager.playerOwner
                .getOrDefault(player.getUniqueId(), Collections.emptyList());

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

        List<GuiItem> claimButtons = buildClaimItems(player, claims);

        ChestGui gui = new ChestGui(6, ComponentHolder.of(Message.msg(player, "messages.claims.ui.select-menu.title")));

        PaginatedPane pages = new PaginatedPane(0, 0, 9, 5);

        pages.populateWithGuiItems(claimButtons);

        StaticPane bottomBar = UIUtil.BOTTOM_BAR.copy();

        bottomBar.addItem(UIUtil.EXIT_BUTTON, 0, 0);

        var pageIndicator = UIUtil.makePageIndicator(gui, player, pages);

        bottomBar.addItem(
            pageIndicator, 4, 0
        );

        gui.addPane(pages);
        gui.addPane(bottomBar);

        gui.show(player);

        UIUtil.updatePageUI(gui, player, pages, bottomBar, pageIndicator);
    }

    private static List<GuiItem> buildClaimItems(Player player, List<Claim> claims) {
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
                plugin.sendMessage(player, Component.translatable("messages.claims.ui.errors.no-item-meta"));
                continue;
            }

            meta.addItemFlags(
                    ItemFlag.HIDE_ATTRIBUTES,
                    ItemFlag.HIDE_ADDITIONAL_TOOLTIP
            );

            meta.setDisplayName(LEGACY_COMPONENT_SERIALIZER.serialize(Component.text(claim.getName(), NamedTextColor.GREEN)));

            List<String> lore = new ArrayList<>();
            String typeMessage;
            switch (claim.getType()) {
                case PLAYER -> typeMessage = LEGACY_COMPONENT_SERIALIZER.serialize(Message.msg(player, "messages.claims.ui.select-menu.player-type"));
                case TEAM -> typeMessage = LEGACY_COMPONENT_SERIALIZER.serialize(Message.msg(player, "messages.claims.ui.select-menu.team-type"));
                case SERVER -> typeMessage = LEGACY_COMPONENT_SERIALIZER.serialize(Message.msg(player, "messages.claims.ui.select-menu.server-type"));
                case PLACEHOLDER -> typeMessage = LEGACY_COMPONENT_SERIALIZER.serialize(Message.msg(player, "messages.claims.ui.select-menu.placeholder-type"));
                default -> typeMessage = LEGACY_COMPONENT_SERIALIZER.serialize(Message.msg(player, "messages.claims.ui.select-menu.unknown-type"));
            }

            lore.add(typeMessage);

            lore.add(LEGACY_COMPONENT_SERIALIZER.serialize(Message.msg(player,
                    "messages.claims.ui.select-menu.id",
                    Map.of("id", String.valueOf(claim.getId())))));

            meta.setLore(lore);
            item.setItemMeta(meta);

            items.add(new GuiItem(item, event -> {
                event.setCancelled(true);
                CoreMain.soundManager.playSound(player, SoundUtil.uiClick);
                ClaimEditMenu.open(player, claim);
            }));
        }

        return items;
    }

//    private static void updatePageIndicator(Player player,
//                                            StaticPane controls,
//                                            int currentPage,
//                                            int totalPages,
//                                            PaginatedPane pages,
//                                            ChestGui gui) {
//        // Clear old navigation items
//        controls.removeItem(2, 0);
//        controls.removeItem(4, 0);
//        controls.removeItem(6, 0);
//
//        // --- Page indicator (middle) ---
//        ItemStack indicator = makePageIndicator(player, currentPage + 1, totalPages);
//        controls.addItem(new GuiItem(indicator, e -> e.setCancelled(true)), 4, 0);
//
//        // --- Previous Button ---
//        if (currentPage > 0) {
//            ItemStack prev = createNavItem(player, "messages.claims.ui.general.previous");
//            controls.addItem(new GuiItem(prev, e -> {
//                e.setCancelled(true);
//                CoreMain.soundManager.playSound(player, UIUtil.CLICK_SOUND);
//                if (pages.getPage() > 0) {
//                    pages.setPage(pages.getPage() - 1);
//                    updatePageIndicator(player, controls, pages.getPage(), totalPages, pages, gui);
//                    gui.update();
//                }
//            }), 2, 0);
//        } else {
//            // Replace with gray filler
//            controls.addItem(new GuiItem(createDisabledNavItem(player), UIUtil.noInteract), 2, 0);
//        }
//
//        // --- Next Button ---
//        if (currentPage < totalPages - 1) {
//            ItemStack next = createNavItem(player, "messages.claims.ui.general.next");
//            controls.addItem(new GuiItem(next, e -> {
//                e.setCancelled(true);
//                CoreMain.soundManager.playSound(player, UIUtil.CLICK_SOUND);
//                if (pages.getPage() < pages.getPages() - 1) {
//                    pages.setPage(pages.getPage() + 1);
//                    updatePageIndicator(player, controls, pages.getPage(), totalPages, pages, gui);
//                    gui.update();
//                }
//            }), 6, 0);
//        } else {
//            // Replace with gray filler
//            controls.addItem(new GuiItem(createDisabledNavItem(player), UIUtil.noInteract), 6, 0);
//        }
//    }

}