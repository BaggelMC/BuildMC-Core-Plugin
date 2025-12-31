package net.mathias2246.buildmc.ui;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.item.ItemUtil;
import net.mathias2246.buildmc.inventoryframework.gui.GuiItem;
import net.mathias2246.buildmc.inventoryframework.gui.type.util.Gui;
import net.mathias2246.buildmc.inventoryframework.pane.PaginatedPane;
import net.mathias2246.buildmc.inventoryframework.pane.StaticPane;
import net.mathias2246.buildmc.util.Message;
import net.mathias2246.buildmc.util.SoundUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static net.mathias2246.buildmc.CoreMain.plugin;

public class UIUtil {

    public static final @NotNull LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.legacySection();

    public static final @NotNull GuiItem BLOCKED_PANE;
    public static final @NotNull GuiItem INVISIBLE_PANE;
    public static final @NotNull GuiItem EXIT_BUTTON;

    public static final @NotNull NamespacedKey INVISIBLE_ITEM_KEY = Objects.requireNonNull(NamespacedKey.fromString("buildmc:none"));

    public final static Consumer<InventoryClickEvent> noInteract = event -> event.setCancelled(true);

    public final static Consumer<InventoryClickEvent> exitButton = event -> {
        event.setCancelled(true);
        event.getWhoClicked().closeInventory();
    };

    public static final StaticPane BOTTOM_BAR_WITH_EXIT;
    public static final StaticPane BOTTOM_BAR;

    static {
        var i1 = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        var i2 = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);

        ItemUtil.editMeta(
                i1,
                (meta) -> {
                    meta.setItemName(null);
                    meta.setHideTooltip(true);
                }
        );

        ItemUtil.editMeta(
                i2,
                (meta) -> {
                    meta.setItemName(null);
                    meta.setHideTooltip(true);
                }
        );

        var e = new ItemStack(Material.BARRIER);
        ItemUtil.editMeta(
                e,
                (meta) -> {
                    meta.setItemName(null);
                    // Component.translatable("ui.nations.general.exit").color(TextColor.color(240, 64, 45))
                    meta.setRarity(ItemRarity.COMMON);
                    meta.setHideTooltip(true);
                }
        );

        var lp = new ItemStack(Material.ARROW);
        ItemUtil.editMeta(
                lp,
                (meta) -> {
                    meta.setItemName(null);
                    // Component.translatable("ui.nations.general.last_page")
                }
        );

        var np = new ItemStack(Material.ARROW);
        ItemUtil.editMeta(
                np,
                (meta) -> {
                    meta.setItemName(null);
                    // Component.translatable("ui.nations.general.next_page")
                }
        );

        BLOCKED_PANE = new GuiItem(i1, noInteract);
        INVISIBLE_PANE = new GuiItem(i2, noInteract);
        EXIT_BUTTON = new GuiItem(e, exitButton);

        LAST_PAGE_BUTTON = new GuiItem(lp, noInteract);
        NEXT_PAGE_BUTTON = new GuiItem(np, noInteract);

        BOTTOM_BAR_WITH_EXIT = new StaticPane(
                0,5,9,1
        );

        BOTTOM_BAR = new StaticPane(
                0,5,9,1
        );

        BOTTOM_BAR_WITH_EXIT.addItem(BLOCKED_PANE, 0, 0);
        BOTTOM_BAR_WITH_EXIT.addItem(BLOCKED_PANE, 1, 0);
        BOTTOM_BAR_WITH_EXIT.addItem(BLOCKED_PANE, 2, 0);
        BOTTOM_BAR_WITH_EXIT.addItem(BLOCKED_PANE, 3, 0);
        BOTTOM_BAR_WITH_EXIT.addItem(EXIT_BUTTON, 4, 0);
        BOTTOM_BAR_WITH_EXIT.addItem(BLOCKED_PANE, 5, 0);
        BOTTOM_BAR_WITH_EXIT.addItem(BLOCKED_PANE, 6, 0);
        BOTTOM_BAR_WITH_EXIT.addItem(BLOCKED_PANE, 7, 0);
        BOTTOM_BAR_WITH_EXIT.addItem(BLOCKED_PANE, 8, 0);

        BOTTOM_BAR.addItem(BLOCKED_PANE, 0, 0);
        BOTTOM_BAR.addItem(BLOCKED_PANE, 1, 0);
        BOTTOM_BAR.addItem(BLOCKED_PANE, 2, 0);
        BOTTOM_BAR.addItem(BLOCKED_PANE, 3, 0);
        BOTTOM_BAR.addItem(BLOCKED_PANE, 4, 0);
        BOTTOM_BAR.addItem(BLOCKED_PANE, 5, 0);
        BOTTOM_BAR.addItem(BLOCKED_PANE, 6, 0);
        BOTTOM_BAR.addItem(BLOCKED_PANE, 7, 0);
        BOTTOM_BAR.addItem(BLOCKED_PANE, 8, 0);
    }



    public static void updatePageUI(Gui gui, Player player, PaginatedPane pages, StaticPane bottomBar, GuiItem pageIndicator) {
        int page = pages.getPage();
        int pagesCount = pages.getPages();

        if (page > 0)
            bottomBar.addItem(UIUtil.makePageLeftButton(gui, player, pages, bottomBar, pageIndicator), 2, 0);
        else
            bottomBar.addItem(UIUtil.BLOCKED_PANE, 2, 0);

        if (page < pagesCount-1)
            bottomBar.addItem(UIUtil.makePageRightButton(gui, player, pages, bottomBar, pageIndicator), 6, 0);
        else
            bottomBar.addItem(UIUtil.BLOCKED_PANE, 6, 0);

        setPageIndicatorName(pageIndicator.getItem(), player, pages);

        gui.update();
    }

    public static void setPageIndicatorName(@NotNull ItemStack itemStack, Player player, PaginatedPane pages) {
        ItemUtil.editMeta(itemStack, meta -> meta.setItemName(LEGACY_COMPONENT_SERIALIZER.serialize(Message.msg(
                player,
                "messages.claims.ui.general.page-indicator",
                Map.of(
                        "current", Integer.toString(pages.getPage()+1),
                        "total", Integer.toString(pages.getPages())
                )
        )))
        );
    }

    public static GuiItem makePageIndicator(Gui gui, @NotNull Player player, PaginatedPane pages) {
        ItemStack itemStack = new ItemStack(Material.PAPER, 1);
        setPageIndicatorName(itemStack, player, pages);
        return new GuiItem(itemStack, noInteract, plugin);
    }

    public static GuiItem makePageLeftButton(Gui gui, Player player, PaginatedPane pages, StaticPane bottomBar, GuiItem pageIndicator) {
        ItemStack itemStack = new ItemStack(Material.ARROW, 1);
        ItemUtil.editMeta(itemStack, meta -> meta.setItemName(LEGACY_COMPONENT_SERIALIZER.serialize(Message.msg(
                player,
                "messages.claims.ui.general.previous")))
        );
        return new GuiItem(itemStack,
                event -> {
                    event.setCancelled(true);
                    int page = pages.getPage();
                    if (page < 0) return;
                    pages.setPage(page - 1);
                    CoreMain.soundManager.playSound(player, SoundUtil.uiClick);
                    updatePageUI(gui, player, pages, bottomBar, pageIndicator);
        },
                plugin
        );
    }
    public static GuiItem makePageRightButton(Gui gui, Player player, PaginatedPane pages, StaticPane bottomBar, GuiItem pageIndicator) {
        ItemStack itemStack = new ItemStack(Material.ARROW, 1);
        ItemUtil.editMeta(itemStack, meta -> meta.setItemName(LEGACY_COMPONENT_SERIALIZER.serialize(Message.msg(
                player,
                "messages.claims.ui.general.next")))
        );
        return new GuiItem(itemStack,
                event -> {
                    event.setCancelled(true);
                    int page = pages.getPage();
                    if (page >= pages.getPages()-1) return;
                    pages.setPage(page + 1);
                    CoreMain.soundManager.playSound(player, SoundUtil.uiClick);
                    updatePageUI(gui, player, pages, bottomBar, pageIndicator);
            },
                plugin
        );
    }

    public static final @NotNull GuiItem LAST_PAGE_BUTTON;
    public static final @NotNull GuiItem NEXT_PAGE_BUTTON;


    public static GuiItem makeGuiItem(@NotNull ItemStack item, Consumer<InventoryClickEvent> onClick) {
        return new GuiItem(item, onClick, plugin);
    }
}
