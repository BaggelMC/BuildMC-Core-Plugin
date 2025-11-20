package net.mathias2246.buildmc.ui.claims;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.claims.ClaimType;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.inventoryframework.adventuresupport.ComponentHolder;
import net.mathias2246.buildmc.inventoryframework.gui.GuiItem;
import net.mathias2246.buildmc.inventoryframework.gui.type.ChestGui;
import net.mathias2246.buildmc.inventoryframework.pane.StaticPane;
import net.mathias2246.buildmc.ui.UIUtil;
import net.mathias2246.buildmc.util.Message;
import net.mathias2246.buildmc.util.SoundUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
import java.util.function.Consumer;

public class ClaimEditMenu {

    public static void open(Player player, Claim claim) {

        boolean isPlaceholderClaim = claim.getType() == ClaimType.PLACEHOLDER;

        ChestGui gui = new ChestGui(3,
                ComponentHolder.of(Message.msg(player, "messages.claims.ui.edit-menu.title",
                        Map.of("claim", claim.getName()))));

        StaticPane pane = new StaticPane(0, 0, 9, 3);

        // Filler
        ItemStack filler = createGlassPane(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        GuiItem fillerItem = new GuiItem(filler, e -> e.setCancelled(true));
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 3; y++) {
                pane.addItem(fillerItem, x, y);
            }
        }

        // Protections button
        if (!CoreMain.plugin.getConfig().getBoolean("claims.hide-all-protections") && !isPlaceholderClaim) {
            pane.addItem(makeButton(Material.SHIELD, Message.msg(player, "messages.claims.ui.edit-menu.protections"),
                    e -> {
                        e.setCancelled(true);
                        CoreMain.soundManager.playSound(player, UIUtil.CLICK_SOUND);
                        ProtectionsMenu.open(player, claim);
                    }), 2, 1);
        } else {
            pane.addItem(makeButton(Material.RED_STAINED_GLASS_PANE, Message.msg(player, "messages.claims.ui.edit-menu.no-protections-available"),
                    e -> e.setCancelled(true)), 2, 1);
        }


        // Whitelist button
        if (!isPlaceholderClaim) {
            pane.addItem(makeButton(Material.PLAYER_HEAD, Message.msg(player, "messages.claims.ui.edit-menu.whitelist"),
                    e -> {
                        e.setCancelled(true);
                        CoreMain.soundManager.playSound(player, UIUtil.CLICK_SOUND);
                        WhitelistMenu.open(player,claim);
                    }), 4, 1);
        } else {
            pane.addItem(makeButton(Material.RED_STAINED_GLASS_PANE, Message.msg(player, "messages.claims.ui.edit-menu.no-whitelist-available"),
                    e -> e.setCancelled(true)), 4, 1);
        }

        // Delete button → opens confirmation menu
        pane.addItem(makeButton(Material.BARRIER, Message.msg(player, "messages.claims.ui.edit-menu.delete"),
                e -> {
                    e.setCancelled(true);
                    CoreMain.soundManager.playSound(player, SoundUtil.notification);
                    openDeleteConfirmationMenu(player, claim);
                }), 6, 1);

        // Delete button → opens confirmation menu
        pane.addItem(makeButton(Material.BARRIER, Message.msg(player, "messages.claims.ui.general.back"),
                e -> {
                    e.setCancelled(true);
                    CoreMain.soundManager.playSound(player, UIUtil.CLICK_SOUND);
                    ClaimSelectMenu.open(player);
                }), 8, 2);


        gui.addPane(pane);
        gui.show(player);
    }

    private static void openDeleteConfirmationMenu(Player player, Claim claim) {

        if (claim.getId() == null) return;

        ChestGui gui = new ChestGui(3,
                ComponentHolder.of(Message.msg(player, "messages.claims.ui.edit-menu.delete-confirm-menu.title", Map.of("claim", claim.getName()))));

        StaticPane pane = new StaticPane(0, 0, 9, 3);

        // Filler
        ItemStack filler = createGlassPane(Material.RED_STAINED_GLASS_PANE);
        GuiItem fillerItem = new GuiItem(filler, e -> e.setCancelled(true));
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 3; y++) {
                pane.addItem(fillerItem, x, y);
            }
        }

        // Cancel
        pane.addItem(makeButton(Material.GREEN_CONCRETE, Message.msg(player, "messages.claims.ui.general.cancel"),
                e -> {
                    e.setCancelled(true);
                    CoreMain.soundManager.playSound(player, UIUtil.CLICK_SOUND);
                    open(player, claim); // reopen edit menu
                }), 3, 1);

        // Confirm
        pane.addItem(makeButton(Material.RED_CONCRETE, Message.msg(player, "messages.claims.ui.general.confirm"),
                e -> {
                    e.setCancelled(true);
                    CoreMain.soundManager.playSound(player, UIUtil.CLICK_SOUND);
                    boolean removed = ClaimManager.removeClaimById(claim.getId());
                    if (removed) {
                        CoreMain.mainClass.sendMessage(player,
                                Component.translatable("messages.claims.ui.edit-menu.delete-confirm-menu.success"));
                    } else {
                        CoreMain.mainClass.sendMessage(player,
                                Component.translatable("messages.claims.ui.edit-menu.delete-confirm-menu.fail"));
                    }
                    player.closeInventory();
                }), 5, 1);

        gui.addPane(pane);
        gui.show(player);
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
