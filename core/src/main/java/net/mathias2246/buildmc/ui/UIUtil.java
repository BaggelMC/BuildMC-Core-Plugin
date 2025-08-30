package net.mathias2246.buildmc.ui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import net.mathias2246.buildmc.api.item.ItemUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

import static net.mathias2246.buildmc.CoreMain.plugin;

public class UIUtil {
    public static final @NotNull GuiItem BLOCKED_PANE;
    public static final @NotNull GuiItem INVISIBLE_PANE;
    public static final @NotNull GuiItem EXIT_BUTTON;

    public static final @NotNull NamespacedKey INVISIBLE_ITEM_KEY = Objects.requireNonNull(NamespacedKey.fromString("nations:none"));

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


    public static final @NotNull GuiItem LAST_PAGE_BUTTON;
    public static final @NotNull GuiItem NEXT_PAGE_BUTTON;


    public static GuiItem makeGuiItem(@NotNull ItemStack item, Consumer<InventoryClickEvent> onClick) {
        return new GuiItem(item, onClick, plugin);
    }
}
