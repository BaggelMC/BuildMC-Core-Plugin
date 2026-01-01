package net.mathias2246.buildmc.ui.claims;

import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.api.item.ItemUtil;
import net.mathias2246.buildmc.claims.ClaimLogger;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.inventoryframework.adventuresupport.ComponentHolder;
import net.mathias2246.buildmc.inventoryframework.gui.GuiItem;
import net.mathias2246.buildmc.inventoryframework.gui.type.ChestGui;
import net.mathias2246.buildmc.inventoryframework.pane.PaginatedPane;
import net.mathias2246.buildmc.inventoryframework.pane.Pane;
import net.mathias2246.buildmc.inventoryframework.pane.StaticPane;
import net.mathias2246.buildmc.ui.UIUtil;
import net.mathias2246.buildmc.util.Message;
import net.mathias2246.buildmc.util.SoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.mathias2246.buildmc.CoreMain.plugin;

public class ProtectionsMenu {

    private static final @NotNull StaticPane SPACER_ROW;

    private static final @NotNull StaticPane INVISIBLE_BACKGROUND;

    static {
        SPACER_ROW = new StaticPane(0, 2, 9, 5);

        INVISIBLE_BACKGROUND = new StaticPane(9,6);

        INVISIBLE_BACKGROUND.setPriority(Pane.Priority.LOWEST);

        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 5; y++) {
                INVISIBLE_BACKGROUND.addItem(UIUtil.INVISIBLE_PANE, x, y);
            }
        }

        SPACER_ROW.setPriority(Pane.Priority.HIGH);

        for (int x = 0; x < 9; x++) {
            SPACER_ROW.addItem(UIUtil.BLOCKED_PANE, x, 0);
        }
    }

    public static void open(Player player, Claim claim) {

        Bukkit.getScheduler().runTaskAsynchronously(plugin, task ->
                {
                    List<Protection> protections = CoreMain.protectionsRegistry.stream().toList();

                    ChestGui gui = new ChestGui(6, ComponentHolder.of(Message.msg(player, "messages.claims.ui.protections-menu.title", Map.of("claim", claim.getName()))));

                    gui.setOnClose(event -> {
                        if (ClaimUIs.openUIs.containsKey(claim.getId())) ClaimUIs.openUIs.get(claim.getId()).remove(player);
                    });

                    PaginatedPane pages = new PaginatedPane(0, 0, 9, 5);

                    boolean isFirstPage = true;
                    boolean lowerRow = false;
                    int column = 0;

                    List<StaticPane> p = new ArrayList<>(); // List of all page panes

                    p.add(new StaticPane(9, 5));
                    StaticPane currentPane = p.getFirst();

                    pages.addPane(0, SPACER_ROW);
                    pages.addPane(0, INVISIBLE_BACKGROUND);
                    pages.addPane(0, currentPane);

                    for (var protection : protections) {
                            if (protection.isHidden()) continue;

                            int row = lowerRow ? 3 : 0;

                            currentPane.addItem(protection.getDisplay(player, gui), column, row);
                            currentPane.addItem(makeStatusItem(gui, currentPane, claim, protection, column, row + 1, player), column, row + 1);

                            column++;


                            if (column >= 9) {
                                column = 0;
                                if (!lowerRow) {
                                    lowerRow = true;
                                } else {
                                    lowerRow = false;
                                    if (!isFirstPage) {
                                        pages.addPage(currentPane);

                                    }
                                        isFirstPage = false;
                                        p.add(new StaticPane(9, 5));
                                        currentPane = p.getLast();
                                        pages.addPane(pages.getPages() - 1, SPACER_ROW);
                                        pages.addPane(pages.getPages() - 1, INVISIBLE_BACKGROUND);
                                }
                            }

                    }
                    if (column > 0 || lowerRow) {
                        if (!isFirstPage) {
                            pages.addPage(currentPane);

                        }
                        pages.addPane(pages.getPages() - 1, SPACER_ROW);
                        pages.addPane(pages.getPages() - 1, INVISIBLE_BACKGROUND);
                    }

                    gui.addPane(pages);

                    // Nav bar
                    StaticPane controls = UIUtil.BOTTOM_BAR.copy();
                    controls.setPriority(Pane.Priority.HIGH);

                    // Back button
                    controls.addItem(new GuiItem(ItemUtil.setItemLegacyComponentName(Material.BARRIER, Message.msg(player, "messages.claims.ui.general.back")), e -> {
                        e.setCancelled(true);
                        CoreMain.soundManager.playSound(player, SoundUtil.uiClick);
                        ClaimEditMenu.open(player, claim);
                    }), 8, 0);

                    var pageIndicator = UIUtil.makePageIndicator(gui, player, pages);
                    controls.addItem(pageIndicator, 4, 0);
                    controls.addItem(UIUtil.makePageLeftButton(gui, player, pages, controls, pageIndicator), 2, 0);
                    controls.addItem(UIUtil.makePageRightButton(gui, player, pages, controls, pageIndicator), 6, 0);

                    gui.addPane(controls);

                    Bukkit.getScheduler().runTask(plugin, bukkitTask ->
                        {
                            gui.show(player);
                            //ClaimUIs.openUIs.put(claim.getId(), player);
                            UIUtil.updatePageUI(gui, player, pages, controls, pageIndicator);

                            Long id = claim.getId();
                            if (id == null) return;

                            if (!ClaimUIs.openUIs.containsKey(id)) {
                                var l = new ArrayList<Player>();
                                l.add(player);
                                ClaimUIs.openUIs.put(id, l);
                            }
                            else ClaimUIs.openUIs.get(id).add(player);
                        }
                    );
                }
        );
    }

    private static GuiItem makeStatusItem(ChestGui gui, StaticPane pane,
                                          Claim claim, Protection protection,
                                          int x, int y, @NotNull Player player) {

        boolean enabled = claim.hasProtection(protection);
        ItemStack status = createStatusPane(enabled, player);

        return new GuiItem(status, event -> {
            event.setCancelled(true);
            CoreMain.soundManager.playSound(player, SoundUtil.uiClick);

            boolean currentlyEnabled = claim.hasProtection(protection);

            if (claim.getId() == null) return;

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

            pane.addItem(updated, x, y);

            gui.update();
        });
    }

    private static ItemStack createStatusPane(boolean enabled, @NotNull Player player) {
        Material color = enabled ? Material.RED_STAINED_GLASS_PANE : Material.GREEN_STAINED_GLASS_PANE;
        return ItemUtil.setItemLegacyComponentName(
                color,
                Message.msg(player, enabled ? "messages.claims.ui.protections-menu.enabled" : "messages.claims.ui.protections-menu.disabled")
        );
    }
}
