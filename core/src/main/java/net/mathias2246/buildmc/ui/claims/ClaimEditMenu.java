package net.mathias2246.buildmc.ui.claims;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.claims.ClaimType;
import net.mathias2246.buildmc.api.item.ItemUtil;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.ui.UIUtil;
import net.mathias2246.buildmc.util.Message;
import net.mathias2246.buildmc.util.SoundUtil;
import net.mathias2246.commons.com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import net.mathias2246.commons.com.github.stefvanschie.inventoryframework.gui.GuiItem;
import net.mathias2246.commons.com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import net.mathias2246.commons.com.github.stefvanschie.inventoryframework.pane.Pane;
import net.mathias2246.commons.com.github.stefvanschie.inventoryframework.pane.StaticPane;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Consumer;

import static net.mathias2246.buildmc.CoreMain.plugin;

public class ClaimEditMenu {

    public static final @NotNull StaticPane BACKGROUND;
    public static final @NotNull StaticPane RED_BACKGROUND;

    private static final boolean HIDE_ALL_PROTECTIONS;

    static {
        BACKGROUND = new StaticPane(9, 3);
        BACKGROUND.setPriority(Pane.Priority.LOWEST);

        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 3; y++) {
                BACKGROUND.addItem(UIUtil.INVISIBLE_PANE, x, y);
            }
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

        HIDE_ALL_PROTECTIONS = plugin.getConfig().getBoolean("claims.hide-all-protections");
    }


    public static void open(@NotNull Player player, @NotNull Claim claim) {

        if (claim.getId() == null) {
            plugin.sendMessage(player, Component.translatable("messages.claims.remove.not-found"));
            plugin.getSoundManager().playSound(player, SoundUtil.mistake);
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, task -> {
                    boolean isPlaceholderClaim = claim.getType() == ClaimType.PLACEHOLDER;

                    ChestGui gui = new ChestGui(3,
                            ComponentHolder.of(Message.msg(player, "messages.claims.ui.edit-menu.title",
                                    Map.of("claim", claim.getName()))));

                    gui.setOnClose(event -> {
                        if (ClaimUIs.openUIs.containsKey(claim.getId())) ClaimUIs.openUIs.get(claim.getId()).remove(player);
                    });

                    StaticPane elements = new StaticPane(9, 3);

                    // Protections button
                    if (!HIDE_ALL_PROTECTIONS && !isPlaceholderClaim) {
                        elements.addItem(makeButton(Material.SHIELD, Message.msg(player, "messages.claims.ui.edit-menu.protections"),
                                e -> {
                                    e.setCancelled(true);
                                    CoreMain.soundManager.playSound(player, SoundUtil.uiClick);
                                    ProtectionsMenu.open(player, claim);
                                }), 2, 1);
                    } else {
                        elements.addItem(makeButton(Material.RED_STAINED_GLASS_PANE, Message.msg(player, "messages.claims.ui.edit-menu.no-protections-available"),
                                e -> e.setCancelled(true)), 2, 1);
                    }

                    // Whitelist button
                    if (!isPlaceholderClaim) {
                        elements.addItem(makeButton(Material.PLAYER_HEAD, Message.msg(player, "messages.claims.ui.edit-menu.whitelist"),
                                e -> {
                                    e.setCancelled(true);
                                    CoreMain.soundManager.playSound(player, SoundUtil.uiClick);
                                    WhitelistMenu.open(player, claim);
                                }), 4, 1);
                    } else {
                        elements.addItem(makeButton(Material.RED_STAINED_GLASS_PANE, Message.msg(player, "messages.claims.ui.edit-menu.no-whitelist-available"),
                                e -> e.setCancelled(true)), 4, 1);
                    }

                    // Delete button → opens confirmation menu
                    elements.addItem(makeButton(Material.BARRIER, Message.msg(player, "messages.claims.ui.edit-menu.delete"),
                            e -> {
                                e.setCancelled(true);
                                CoreMain.soundManager.playSound(player, SoundUtil.uiClick);
                                openDeleteConfirmationMenu(player, claim);
                            }), 6, 1);

                    // Delete button → opens confirmation menu
                    elements.addItem(makeButton(Material.BARRIER, Message.msg(player, "messages.claims.ui.general.back"),
                            e -> {
                                e.setCancelled(true);
                                CoreMain.soundManager.playSound(player, SoundUtil.uiClick);
                                ClaimSelectMenu.open(player);
                            }), 8, 2);

                    gui.addPane(BACKGROUND);
                    gui.addPane(elements);

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
                    }
                    );
                }
        );


    }

    private static void openDeleteConfirmationMenu(Player player, Claim claim) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task -> {
                    if (claim.getId() == null) return;
                    ChestGui gui = new ChestGui(3,
                            ComponentHolder.of(Message.msg(player, "messages.claims.ui.edit-menu.delete-confirm-menu.title", Map.of("claim", claim.getName()))));

                    gui.setOnClose(event -> {
                        if (ClaimUIs.openUIs.containsKey(claim.getId())) ClaimUIs.openUIs.get(claim.getId()).remove(player);
                    });

                    StaticPane elements = new StaticPane(9, 3);

                    // Cancel
                    elements.addItem(makeButton(Material.GREEN_CONCRETE, Message.msg(player, "messages.claims.ui.general.cancel"),
                            e -> {
                                e.setCancelled(true);
                                CoreMain.soundManager.playSound(player, SoundUtil.uiClick);
                                open(player, claim); // reopen edit menu
                            }), 3, 1);

                    // Confirm
                    elements.addItem(makeButton(Material.RED_CONCRETE, Message.msg(player, "messages.claims.ui.general.confirm"),
                            e -> {
                                e.setCancelled(true);
                                CoreMain.soundManager.playSound(player, SoundUtil.uiClick);
                                boolean removed = ClaimManager.removeClaimById(claim.getId());
                                if (removed) {
                                    plugin.sendMessage(player,
                                            Component.translatable("messages.claims.ui.edit-menu.delete-confirm-menu.success"));
                                } else {
                                    plugin.sendMessage(player,
                                            Component.translatable("messages.claims.ui.edit-menu.delete-confirm-menu.fail"));
                                }
                                player.closeInventory();
                            }), 5, 1);

                    gui.addPane(RED_BACKGROUND);
                    gui.addPane(elements);

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

    private static GuiItem makeButton(Material material, Component name,
                                      Consumer<org.bukkit.event.inventory.InventoryClickEvent> action) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(name));
            item.setItemMeta(meta);
        }
        return new GuiItem(item, action);
    }

    private static ItemStack createGlassPane(Material material) {
        ItemStack pane = new ItemStack(material);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.setHideTooltip(true);
            pane.setItemMeta(meta);
        }
        return pane;
    }
}
