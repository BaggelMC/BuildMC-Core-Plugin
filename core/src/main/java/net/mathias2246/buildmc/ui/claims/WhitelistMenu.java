package net.mathias2246.buildmc.ui.claims;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import com.google.common.collect.ImmutableSet;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.item.ItemUtil;
import net.mathias2246.buildmc.claims.ClaimLogger;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.ui.SignInputScreen;
import net.mathias2246.buildmc.ui.UIUtil;
import net.mathias2246.buildmc.util.AudienceUtil;
import net.mathias2246.buildmc.util.Message;
import net.mathias2246.buildmc.util.SoundUtil;
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

    static {
        SPACER_ROW = new StaticPane(9, 5);

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
                    ImmutableSet<UUID> whitelist = claim.getWhitelistedPlayers();

                    ChestGui gui = new ChestGui(6, ComponentHolder.of(Message.msg(player, "messages.claims.ui.whitelist-menu.title", Map.of("claim", claim.getName()))));

                    gui.setOnClose(event -> {
                        if (ClaimUIs.openUIs.containsKey(claim.getId())) ClaimUIs.openUIs.get(claim.getId()).remove(player);
                    });

                    PaginatedPane pages = new PaginatedPane(9, 5);

                    boolean isFirstPage = true;
                    boolean lowerRow = false;
                    int column = 0;

                    List<StaticPane> p = new ArrayList<>(); // List of all page panes

                    p.add(new StaticPane(9, 5));
                    StaticPane currentPane = p.getFirst();

                    pages.addPane(0, Slot.fromXY(0, 2), SPACER_ROW);
                    pages.addPane(0, Slot.fromXY(0, 0), INVISIBLE_BACKGROUND);

                    pages.addPane(0, Slot.fromXY(0, 0), currentPane);

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
                        ItemStack paneItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                        ItemUtil.setName(paneItem, Message.msg(player, "messages.claims.ui.whitelist-menu.remove"));
                        currentPane.addItem(

                                new GuiItem
                                        (
                                                paneItem,
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

                                    pages.addPage(Slot.fromXY(0, 0), currentPane);
                                }
                                isFirstPage = false;
                                p.add(new StaticPane(9, 5));
                                currentPane = p.getLast();
                                pages.addPane(pages.getPages() - 1, Slot.fromXY(0, 2), SPACER_ROW);
                                pages.addPane(pages.getPages() - 1, Slot.fromXY(0, 0), INVISIBLE_BACKGROUND);
                            }
                        }

                    }
                    if (column > 0 || lowerRow) {
                        if (!isFirstPage) {
                            pages.addPage(Slot.fromXY(0, 0), currentPane);

                        }
                        pages.addPane(pages.getPages() - 1, Slot.fromXY(0, 2), SPACER_ROW);

                        pages.addPane(pages.getPages() - 1, Slot.fromXY(0, 0), INVISIBLE_BACKGROUND);
                    }

                    gui.addPane(Slot.fromXY(0, 0), pages);
                    StaticPane addButton = new StaticPane(1,1);
                    ItemStack addPaneItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
                    ItemUtil.setName(addPaneItem, Message.msg(player, "messages.claims.ui.whitelist-menu.add-button.name"));
                    addButton.addItem(new GuiItem(addPaneItem, e -> {
                        e.setCancelled(true);
                        CoreMain.soundManager.playSound(player, SoundUtil.notification);

                        Dialog dialog = Dialog.create(builder -> builder.empty()
                                .base(DialogBase.builder(Message.msg(player, "messages.claims.ui.whitelist-menu.add-button.dialog-title"))
                                        .canCloseWithEscape(true)
                                        .inputs(List.of(
                                                DialogInput.text("player_name", Message.msg(player, "messages.claims.ui.whitelist-menu.add-button.input-label"))
                                                        .build()
                                        ))
                                        .build()
                                )
                                .type(DialogType.confirmation(
                                        ActionButton.builder(Message.msg(player, "messages.claims.ui.whitelist-menu.add-button.confirm"))
                                                .action(DialogAction.customClick(
                                                        (view, audience) -> {
                                                            String input = view.getText("player_name");
                                                            if (input == null || input.isBlank()) return;

                                                            Player target = Bukkit.getPlayer(input);
                                                            if (target == null) {
                                                                CoreMain.soundManager.playSound(player, SoundUtil.mistake);
                                                                AudienceUtil.sendMessage(player, Component.translatable("messages.claims.whitelist.player-not-found"));
                                                                return;
                                                            }

                                                            ClaimManager.addPlayerToWhitelist(claim, target.getUniqueId());

                                                            CoreMain.soundManager.playSound(player, SoundUtil.success);
                                                            AudienceUtil.sendMessage(player, Component.translatable("messages.claims.whitelist.added"));

                                                            Bukkit.getScheduler().runTask(plugin, () -> open(player, claim));
                                                        },
                                                        ClickCallback.Options.builder().uses(1).build()
                                                ))
                                                .build(),
                                        ActionButton.builder(Message.msg(player, "messages.claims.ui.whitelist-menu.add-button.cancel"))
                                                .action(null) // null = just close the dialog
                                                .build()
                                ))
                        );

                        player.showDialog(dialog);
                    }), 0, 0);
                    addButton.setPriority(Pane.Priority.HIGH);

                    // Nav bar
                    StaticPane controls = UIUtil.BOTTOM_BAR.copy();
                    controls.setPriority(Pane.Priority.HIGH);

                    // Back button
                    var backItem = new ItemStack(Material.BARRIER);
                    ItemUtil.setName(backItem, Message.msg(player, "messages.claims.ui.general.back"));

                    controls.addItem(new GuiItem(backItem, e -> {
                        e.setCancelled(true);
                        CoreMain.soundManager.playSound(player, SoundUtil.uiClick);
                        ClaimEditMenu.open(player, claim);
                    }), 8, 0);

                    var pageIndicator = UIUtil.makePageIndicator(gui, player, pages);
                    controls.addItem(pageIndicator, 4, 0);
                    controls.addItem(UIUtil.makePageLeftButton(gui, player, pages, controls, pageIndicator), 2, 0);
                    controls.addItem(UIUtil.makePageRightButton(gui, player, pages, controls, pageIndicator), 6, 0);

                    gui.addPane(Slot.fromXY(0, 5), controls);
                    gui.addPane(Slot.fromXY(4,2), addButton);

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

                    StaticPane pane = new StaticPane(9, 3);

                    // Cancel button
                    var cancelButton = new ItemStack(Material.GREEN_CONCRETE);
                    ItemUtil.setName(cancelButton, Message.msg(player, "messages.claims.ui.general.cancel"));

                    pane.addItem(new GuiItem(cancelButton, e -> {
                        e.setCancelled(true);
                        CoreMain.soundManager.playSound(player, SoundUtil.uiClick);
                        open(player, claim); // reopen whitelist menu
                    }), 3, 1);

                    // Confirm button
                    var confirmButton = new ItemStack(Material.RED_CONCRETE);
                    ItemUtil.setName(confirmButton, Message.msg(player, "messages.claims.ui.general.confirm"));

                    pane.addItem(new GuiItem(confirmButton, e -> {
                        e.setCancelled(true);

                        CoreMain.soundManager.playSound(player, SoundUtil.uiClick);

                        UUID uuid = target.getUniqueId();

                        ClaimManager.removePlayerFromWhitelist(claim.getId(), uuid);
                         AudienceUtil.sendMessage(player, Component.translatable("messages.claims.ui.whitelist-menu.delete-confirm-menu.success"));
                        ClaimLogger.logWhitelistRemoved(player, claim.getName(), target.getName(), uuid.toString());
                        open(player, claim);
                    }), 5, 1);

                    gui.addPane(Slot.fromXY(0, 0), pane);
                    gui.addPane(Slot.fromXY(0, 0), RED_BACKGROUND);

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
            meta.displayName(Component.text(Objects.requireNonNull(offlinePlayer.getName()), NamedTextColor.WHITE));
            head.setItemMeta(meta);
        }
        return head;
    }
}
