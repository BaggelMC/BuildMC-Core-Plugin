package net.mathias2246.buildmc.ui.claims;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.item.ItemUtil;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.ui.UIUtil;
import net.mathias2246.buildmc.util.AudienceUtil;
import net.mathias2246.buildmc.util.Message;
import net.mathias2246.buildmc.util.SoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static net.mathias2246.buildmc.CoreMain.plugin;

public class ClaimSelectMenu {

    public static final int SLOTS_PER_PAGE = 45;

    private static final Map<UUID, ClaimSelectMenu> openMenus = new ConcurrentHashMap<>();

    private final Player player;
    private final boolean showAllClaims;
    private ChestGui gui;
    private PaginatedPane pages;
    private StaticPane bottomBar;
    private GuiItem pageIndicator;

    private ClaimSelectMenu(@NotNull Player player) {
        this.player = player;
        this.showAllClaims = player.hasPermission("buildmc.show-all-claims");
    }

    public static void open(@NotNull Player player) {
        ClaimSelectMenu menu = new ClaimSelectMenu(player);
        openMenus.put(player.getUniqueId(), menu);
        menu.openInternal();
    }

    public static void refreshForPlayer(@NotNull Player player) {
        ClaimSelectMenu menu = openMenus.get(player.getUniqueId());
        if (menu != null) {
            // Small delay so ClaimManager reflects the new state before we read it
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task -> menu.refreshInternal(), 1L);
        }
    }

    public static void refreshAll() {
        for (UUID uuid : openMenus.keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                refreshForPlayer(p);
            }
        }
    }

    public static Set<UUID> getOpenMenuUUIDs() {
        return Collections.unmodifiableSet(openMenus.keySet());
    }

    private void openInternal() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task -> {
            List<Claim> claims = fetchClaims();
            List<GuiItem> claimButtons = buildClaimItems(player, claims, showAllClaims);

            gui = new ChestGui(6, ComponentHolder.of(Message.msg(player, "messages.claims.ui.select-menu.title")));

            pages = new PaginatedPane(9, 5);
            pages.populateWithGuiItems(claimButtons);

            StaticPane background = new StaticPane(9, 5);
            background.setPriority(Pane.Priority.LOWEST);
            for (int x = 0; x < 9; x++) {
                for (int y = 0; y < 5; y++) {
                    background.addItem(UIUtil.INVISIBLE_PANE, x, y);
                }
            }
            gui.addPane(Slot.fromXY(0, 0), background);

            bottomBar = UIUtil.BOTTOM_BAR.copy();
            bottomBar.addItem(UIUtil.EXIT_BUTTON, 8, 0);

            ItemStack newClaimItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemUtil.setName(newClaimItem, Message.msg(player, "messages.claims.ui.select-menu.new-claim-button"));
            bottomBar.addItem(new GuiItem(newClaimItem, e -> {
                e.setCancelled(true);
                CoreMain.soundManager.playSound(player, SoundUtil.uiClick);
                Bukkit.getScheduler().runTask(plugin, () -> CreateClaimDialog.open(player));
            }), 0, 0);

            pageIndicator = UIUtil.makePageIndicator(gui, player, pages);
            bottomBar.addItem(pageIndicator, 4, 0);

            gui.addPane(Slot.fromXY(0, 0), pages);
            gui.addPane(Slot.fromXY(0, 5), bottomBar);

            gui.setOnClose(e -> openMenus.remove(player.getUniqueId()));

            Bukkit.getScheduler().runTask(plugin, () -> {
                gui.show(player);
                UIUtil.updatePageUI(gui, player, pages, bottomBar, pageIndicator);
            });
        });
    }

    private void refreshInternal() {
        List<Claim> claims = fetchClaims();
        List<GuiItem> claimButtons = buildClaimItems(player, claims, showAllClaims);

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline() || gui == null) {
                openMenus.remove(player.getUniqueId());
                return;
            }

            int savedPage = pages.getPage();

            pages.clear();
            pages.populateWithGuiItems(claimButtons);

            int maxPage = Math.max(0, pages.getPages() - 1);
            pages.setPage(Math.min(savedPage, maxPage));

            UIUtil.updatePageUI(gui, player, pages, bottomBar, pageIndicator);
            gui.update();
        });
    }

    private List<Claim> fetchClaims() {
        List<Claim> claims = new ArrayList<>();
        List<Long> claimIds = new ArrayList<>();

        if (showAllClaims) {
            for (var playerClaims : ClaimManager.playerOwner.values()) {
                claimIds.addAll(playerClaims);
            }
            for (var teamClaims : ClaimManager.teamOwner.values()) {
                claimIds.addAll(teamClaims);
            }
        } else {
            claimIds.addAll(ClaimManager.playerOwner
                    .getOrDefault(player.getUniqueId(), Collections.emptyList()));

            claimIds.addAll(Optional.ofNullable(ClaimManager.getPlayerTeam(player))
                    .map(t -> ClaimManager.teamOwner.getOrDefault(t.getName(), Collections.emptyList()))
                    .orElse(Collections.emptyList()));
        }

        for (long id : claimIds) {
            Claim c = ClaimManager.getClaimByID(id);
            if (c != null) claims.add(c);
        }

        if (player.hasPermission("buildmc.admin") || showAllClaims) {
            for (long id : ClaimManager.serverClaims) {
                Claim c = ClaimManager.getClaimByID(id);
                if (c != null) claims.add(c);
            }
            for (long id : ClaimManager.placeholderClaims) {
                Claim c = ClaimManager.getClaimByID(id);
                if (c != null) claims.add(c);
            }
        }

        return claims;
    }

    private static List<GuiItem> buildClaimItems(Player player, List<Claim> claims, boolean showOwner) {
        List<GuiItem> items = new ArrayList<>();

        for (Claim claim : claims) {
            Material material = switch (claim.getType()) {
                case PLAYER -> Material.PLAYER_HEAD;
                case TEAM -> Material.IRON_SWORD;
                case SERVER -> Material.COMMAND_BLOCK;
                case PLACEHOLDER -> Material.STRUCTURE_VOID;
                default -> Material.PAPER;
            };

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                AudienceUtil.sendMessage(player, Component.translatable("messages.claims.ui.errors.no-item-meta"));
                continue;
            }

            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.displayName(Component.text(claim.getName(), NamedTextColor.GREEN));

            Component typeMessage = switch (claim.getType()) {
                case PLAYER -> Message.msg(player, "messages.claims.ui.select-menu.player-type");
                case TEAM -> Message.msg(player, "messages.claims.ui.select-menu.team-type");
                case SERVER -> Message.msg(player, "messages.claims.ui.select-menu.server-type");
                case PLACEHOLDER -> Message.msg(player, "messages.claims.ui.select-menu.placeholder-type");
                default -> Message.msg(player, "messages.claims.ui.select-menu.unknown-type");
            };

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Owner: " + ClaimManager.getOwnerName(claim)));
            lore.add(typeMessage);
            lore.add(Message.msg(player, "messages.claims.ui.select-menu.id",
                    Map.of("id", String.valueOf(claim.getId()))));

            meta.lore(lore);
            item.setItemMeta(meta);

            items.add(new GuiItem(item, event -> {
                event.setCancelled(true);
                CoreMain.soundManager.playSound(player, SoundUtil.uiClick);
                ClaimEditMenu.open(player, claim);
            }));
        }

        return items;
    }
}
