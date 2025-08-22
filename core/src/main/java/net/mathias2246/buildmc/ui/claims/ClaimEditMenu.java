package net.mathias2246.buildmc.ui.claims;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.claims.Claim;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
import java.util.function.Consumer;

public class ClaimEditMenu {

    public static void open(Player player, Claim claim) {
        ChestGui gui = new ChestGui(3,
                ComponentHolder.of(Message.msg(player, "messages.claims.ui.edit-menu.title", Map.of("claim", claim.getName()))));

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
        pane.addItem(makeButton(Material.SHIELD, Component.translatable("messages.claims.ui.edit-menu.protections"),
                e -> {
                    e.setCancelled(true);
                    ProtectionsMenu.open(player, claim);
                }), 2, 1);

        // Whitelist button
        pane.addItem(makeButton(Material.PLAYER_HEAD, Component.translatable("messages.claims.ui.edit-menu.whitelist"),
                e -> {
                    e.setCancelled(true);
                    CoreMain.mainClass.sendPlayerMessage(player,
                            Component.text("Open Whitelist Menu (not implemented yet)", NamedTextColor.GRAY));
                }), 4, 1);

        // Delete button
        pane.addItem(makeButton(Material.BARRIER, Component.translatable("messages.claims.ui.edit-menu.delete"),
                e -> {
                    e.setCancelled(true);
                    CoreMain.mainClass.sendPlayerMessage(player,
                            Component.text("Delete Claim (not implemented yet)", NamedTextColor.GRAY));
                }), 6, 1);

        gui.addPane(pane);
        gui.show(player);
    }

    private static GuiItem makeButton(Material material, Component name,
                                      Consumer<org.bukkit.event.inventory.InventoryClickEvent> action) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(LegacyComponentSerializer.legacySection()
                    .serialize(name));
            item.setItemMeta(meta);
        }
        return new GuiItem(item, action);
    }

    @SuppressWarnings("SameParameterValue")
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
