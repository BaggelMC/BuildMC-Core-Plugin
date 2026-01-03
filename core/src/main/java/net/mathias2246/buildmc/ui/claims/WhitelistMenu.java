package net.mathias2246.buildmc.ui.claims;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.item.ItemUtil;
import net.mathias2246.buildmc.claims.ClaimLogger;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.ui.UIUtil;
import net.mathias2246.buildmc.util.Message;
import net.mathias2246.buildmc.util.SoundUtil;
import net.mathias2246.commons.com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import net.mathias2246.commons.com.github.stefvanschie.inventoryframework.gui.GuiItem;
import net.mathias2246.commons.com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import net.mathias2246.commons.com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import net.mathias2246.commons.com.github.stefvanschie.inventoryframework.pane.Pane;
import net.mathias2246.commons.com.github.stefvanschie.inventoryframework.pane.StaticPane;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static net.mathias2246.buildmc.CoreMain.plugin;

public class WhitelistMenu {

    private static final @NotNull StaticPane SPACER_ROW;

    private static final @NotNull StaticPane INVISIBLE_BACKGROUND;
    public static final @NotNull StaticPane RED_BACKGROUND;
    private static final @NotNull ItemStack ADD_BUTTON;

    static {
        ADD_BUTTON = new ItemStack(Material.LIME_STAINED_GLASS_PANE, 1);

        SPACER_ROW = new StaticPane(0, 2, 9, 5);

        INVISIBLE_BACKGROUND = new StaticPane(9, 6);

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

        ItemStack redPaneItem = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
        ItemUtil.editMeta(redPaneItem, meta ->
                meta.setHideTooltip(true)
        );
        GuiItem redPane = new GuiItem(redPaneItem, event -> event.setCancelled(true));
        RED_BACKGROUND = new StaticPane(9, 3);
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 3; y++) {
                RED_BACKGROUND.addItem(redPane, x, y);
            }
        }
        RED_BACKGROUND.setPriority(Pane.Priority.LOWEST);
    }

    public static void open(Player player, Claim claim) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task ->
                {
                    List<UUID> whitelist = claim.getWhitelistedPlayers();

                    ChestGui gui = new ChestGui(6, ComponentHolder.of(Message.msg(player, "messages.claims.ui.whitelist-menu.title", Map.of("claim", claim.getName()))));

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

                    for (var playerUUID : whitelist) {
                        int row = lowerRow ? 3 : 0;

                        OfflinePlayer target = Bukkit.getOfflinePlayer(playerUUID);

                        currentPane.addItem(
                                new GuiItem(createPlayerHead(target),
                                        event -> event.setCancelled(true)
                                ),
                                column,
                                row
                        );
                        currentPane.addItem(
                                new GuiItem
                                        (
                                                ItemUtil.setItemLegacyComponentName(Material.RED_STAINED_GLASS_PANE, Message.msg(player, "messages.claims.ui.whitelist-menu.remove")),
                                                event -> {
                                                    event.setCancelled(true);
                                                    CoreMain.soundManager.playSound(player, SoundUtil.notification);

                                                    openDeleteConfirmationMenu(player, claim, target);
                                                }
                                        ),
                                column,
                                row + 1);

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
                    StaticPane addButton = new StaticPane(4,2,1,1);
                    ItemStack a = ItemUtil.setItemLegacyComponentName(Material.LIME_STAINED_GLASS_PANE, Message.msg(player, "messages.claims.ui.whitelist-menu.add-button.name"));
                    addButton.addItem(new GuiItem(a, e -> {
                        e.setCancelled(true);
                        CoreMain.soundManager.playSound(player, SoundUtil.notification);
                        CoreMain.plugin.sendMessage(player,
                                Component.translatable("messages.claims.ui.whitelist-menu.add-button.click-info")
                                        .clickEvent(
                                                ClickEvent.suggestCommand("/claim whitelist add "+claim.getType()+" "+claim.getName()+" ")
                                        )
                        );
                    }), 0, 0);
                    addButton.setPriority(Pane.Priority.HIGH);

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
                    gui.addPane(addButton);

                    Bukkit.getScheduler().runTask(plugin, bukkitTask ->
                            {
                                gui.show(player);
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

    private static void openDeleteConfirmationMenu(Player player, Claim claim, @NotNull OfflinePlayer target) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task -> {
                    if (claim.getId() == null) return;

                    ChestGui gui = new ChestGui(3,
                            ComponentHolder.of(Message.msg(player, "messages.claims.ui.whitelist-menu.delete-confirm-menu.title", Map.of("player", String.valueOf(target.getName())))));

                    gui.setOnClose(event -> {
                        if (ClaimUIs.openUIs.containsKey(claim.getId())) ClaimUIs.openUIs.get(claim.getId()).remove(player);
                    });

                    StaticPane pane = new StaticPane(0, 0, 9, 3);

                    // Cancel button
                    pane.addItem(new GuiItem(ItemUtil.setItemLegacyComponentName(Material.GREEN_CONCRETE, Message.msg(player, "messages.claims.ui.general.cancel")), e -> {
                        e.setCancelled(true);
                        CoreMain.soundManager.playSound(player, SoundUtil.uiClick);
                        open(player, claim); // reopen whitelist menu
                    }), 3, 1);

                    // Confirm button
                    pane.addItem(new GuiItem(ItemUtil.setItemLegacyComponentName(Material.RED_CONCRETE, Message.msg(player, "messages.claims.ui.general.confirm")), e -> {
                        e.setCancelled(true);

                        CoreMain.soundManager.playSound(player, SoundUtil.uiClick);

                        UUID uuid = target.getUniqueId();

                        ClaimManager.removePlayerFromWhitelist(claim.getId(), uuid);
                        CoreMain.plugin.sendMessage(player, Component.translatable("messages.claims.ui.whitelist-menu.delete-confirm-menu.success"));
                        ClaimLogger.logWhitelistRemoved(player, claim.getName(), target.getName(), uuid.toString());
                        open(player, claim);
                    }), 5, 1);

                    gui.addPane(pane);
                    gui.addPane(RED_BACKGROUND);

                    Bukkit.getScheduler().runTask(plugin, bukkitTask -> {
                        gui.show(player);

                        Long id = claim.getId();
                        if (id == null) return;

                        if (!ClaimUIs.openUIs.containsKey(id)) {
                            var l = new ArrayList<Player>();
                            l.add(player);
                            ClaimUIs.openUIs.put(id, l);
                        }
                        else ClaimUIs.openUIs.get(id).add(player);
                    });
                }
        );
    }

    private static ItemStack createPlayerHead(OfflinePlayer offlinePlayer) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(offlinePlayer);
            meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(Component.text(Objects.requireNonNull(offlinePlayer.getName()), NamedTextColor.WHITE)));
            head.setItemMeta(meta);
        }
        return head;
    }
}
