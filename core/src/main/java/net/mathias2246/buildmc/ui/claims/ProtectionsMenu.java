package net.mathias2246.buildmc.ui.claims;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.mathias2246.buildmc.claims.Claim;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.claims.ProtectionFlag;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ProtectionsMenu {

    public static void open(Player player, Claim claim) {
        ProtectionFlag[] allFlags = ProtectionFlag.values();
        int flagsPerPage = 18; // Two sets of 9 per page
        int totalPages = Math.max(1, (int) Math.ceil((double) allFlags.length / flagsPerPage));

        ChestGui gui = new ChestGui(6, ComponentHolder.of(Message.msg(player, "messages.claims.ui.protections-menu.title", Map.of("claim", claim.getName()))));
        PaginatedPane pages = new PaginatedPane(0, 0, 9, 5);

        for (int page = 0; page < totalPages; page++) {
            StaticPane pane = new StaticPane(0, 0, 9, 5);

            // Fill background
            GuiItem fillerItem = new GuiItem(createGlassPane(Material.LIGHT_GRAY_STAINED_GLASS_PANE), e -> e.setCancelled(true));
            for (int y = 0; y < 5; y++) {
                for (int x = 0; x < 9; x++) {
                    pane.addItem(fillerItem, x, y);
                }
            }

            int start = page * flagsPerPage;
            int end = Math.min(start + flagsPerPage, allFlags.length);
            int index = 0;

            for (int i = start; i < end; i++) {
                ProtectionFlag flag = allFlags[i];
                boolean enabled = claim.getProtectionFlags().contains(flag);

                int x = index % 9;
                int y = (index < 9) ? 0 : 3; // First group or second group
                int statusRow = (index < 9) ? 1 : 4;

                // Add icon
                ItemStack icon = createFlagIcon(player, flag);
                pane.addItem(new GuiItem(icon, e -> e.setCancelled(true)), x, y);

                // Status glass
                ItemStack status = createStatusPane(flag, enabled, player);
                GuiItem statusItem = new GuiItem(status, event -> {
                    event.setCancelled(true);

                    // Toggle the flag
                    toggleFlag(claim, flag, enabled);

                    // Update the new status immediately
                    boolean newEnabled = claim.getProtectionFlags().contains(flag);
                    ItemStack newStatus = createStatusPane(flag, newEnabled, player);

                    // Replace this slot with updated status item
                    pane.addItem(new GuiItem(newStatus, e2 -> {
                        e2.setCancelled(true);
                        // recursively handle further toggles
                        toggleFlag(claim, flag, newEnabled);
                        // refresh this one slot again

                        // ItemStack nextStatus = createStatusPane(flag, nextEnabled, player);

                        pane.addItem(makeStatusItem(gui, pane, claim, flag, x, statusRow, player), x, statusRow);

                        gui.update();
                    }), x, statusRow);

                    // Refresh UI without reopening
                    gui.update();
                });

                pane.addItem(statusItem, x, statusRow);

                index++;
            }

            // Spacer
            for (int x = 0; x < 9; x++) {
                pane.addItem(new GuiItem(createGlassPane(Material.GRAY_STAINED_GLASS_PANE), e -> e.setCancelled(true)), x, 2);
            }

            pages.addPane(page, pane);
        }

        pages.setPage(0);
        gui.addPane(pages);

        // Nav bar
        StaticPane controls = new StaticPane(0, 5, 9, 1);
        GuiItem grayItem = new GuiItem(createGlassPane(Material.GRAY_STAINED_GLASS_PANE), e -> e.setCancelled(true));
        for (int i = 0; i < 9; i++) {
            controls.addItem(grayItem, i, 0);
        }

        // Prev button
        controls.addItem(new GuiItem(createNamedItem(Material.ARROW, Message.msg(player,"messages.claims.ui.general.previous")), e -> {
            e.setCancelled(true);
            if (pages.getPage() > 0) {
                int newPage = pages.getPage() - 1;
                pages.setPage(newPage);
                updatePageIndicator(player, controls, newPage + 1, totalPages);
                gui.update();
            }
        }), 2, 0);

        // Page indicator
        updatePageIndicator(player, controls, pages.getPage() + 1, totalPages);

        // Next button
        controls.addItem(new GuiItem(createNamedItem(Material.ARROW, Message.msg(player,"messages.claims.ui.general.next")), e -> {
            e.setCancelled(true);
            if (pages.getPage() < totalPages - 1) {
                int newPage = pages.getPage() + 1;
                pages.setPage(newPage);
                updatePageIndicator(player, controls, newPage + 1, totalPages);
                gui.update();
            }
        }), 6, 0);

        // Back button
        controls.addItem(new GuiItem(createNamedItem(Material.BARRIER, Message.msg(player,"messages.claims.ui.general.back")), e -> {
            e.setCancelled(true);
            ClaimEditMenu.open(player, claim); // Navigate back to Claim Edit Menu
        }), 8, 0);

        gui.addPane(controls);
        gui.show(player);
    }

    private static GuiItem makeStatusItem(ChestGui gui, StaticPane pane,
                                          Claim claim, ProtectionFlag flag,
                                          int x, int y, @NotNull Player player) {
        boolean enabled = claim.getProtectionFlags().contains(flag);
        ItemStack status = createStatusPane(flag, enabled, player);

        return new GuiItem(status, e -> {
            e.setCancelled(true);

            // Toggle
            if (enabled) {
                ClaimManager.removeProtectionFlag(claim, flag);
            } else {
                ClaimManager.addProtectionFlag(claim, flag);
            }

            // Replace this slot with a freshly built GuiItem (so it keeps toggling)
            GuiItem updated = makeStatusItem(gui, pane, claim, flag, x, y, player);
            pane.addItem(updated, x, y);

            gui.update();
        });
    }


    private static void toggleFlag(Claim claim, ProtectionFlag flag, boolean currentlyEnabled) {
        if (currentlyEnabled) {
            ClaimManager.removeProtectionFlag(claim, flag);
        } else {
            ClaimManager.addProtectionFlag(claim, flag);
        }
    }

    private static ItemStack createFlagIcon(Player player, ProtectionFlag flag) {
        Material iconMaterial = getMaterialForFlag(flag);
        ItemStack item = new ItemStack(iconMaterial);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            Component displayName = Message.msg(player,
                    "messages.claims.ui.protections-menu.flag." + flag.name().toLowerCase() + ".name");
            meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(displayName));

            Component lore = Message.msg(player,
                    "messages.claims.ui.protections-menu.flag." + flag.name().toLowerCase() + ".lore");

            String loreString = LegacyComponentSerializer.legacySection().serialize(lore)
                    .replace("\\n", "\n");

            List<String> loreLines = new ArrayList<>(Arrays.asList(loreString.split("\n")));


            loreLines.add(LegacyComponentSerializer.legacySection().serialize(Message.msg(player,"messages.claims.ui.protections-menu.toggle-hint")));

            meta.setLore(loreLines);

            meta.setTool(null);
            meta.setAttributeModifiers(null);
            meta.setRarity(ItemRarity.COMMON);

            item.setItemMeta(meta);
        }
        return item;
    }


    private static ItemStack createStatusPane(ProtectionFlag flag, boolean enabled, @NotNull Player player) {
        Material color = enabled ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        ItemStack item = new ItemStack(color);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(Message.msg(player, enabled ? "messages.claims.ui.protections-menu.enabled" : "messages.claims.ui.protections-menu.disabled")));
            item.setItemMeta(meta);
        }
        return item;
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

    @SuppressWarnings("SameParameterValue")
    private static ItemStack createNamedItem(Material material, Component name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    private static void updatePageIndicator(Player player, StaticPane controls, int current, int total) {
        controls.removeItem(4, 0);
        ItemStack pageIndicator = new ItemStack(Material.PAPER);
        ItemMeta meta = pageIndicator.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(Message.msg(player, "messages.claims.ui.general.page-indicator", Map.of("current", String.valueOf(current), "total", String.valueOf(total)))));
            pageIndicator.setItemMeta(meta);
        }
        controls.addItem(new GuiItem(pageIndicator, e -> e.setCancelled(true)), 4, 0);
    }

    private static Material getMaterialForFlag(ProtectionFlag flag) {
        return switch (flag) {
            case PLAYER_BREAK -> Material.IRON_PICKAXE;
            case PLAYER_PLACE -> Material.STONE;
            case CONTAINER -> Material.CHEST;
            case ITEM_PICKUP -> Material.HOPPER;
            case ITEM_DROP -> Material.DROPPER;
            case SIGN_EDITING -> Material.OAK_SIGN;
            case SPLASH_POTIONS -> Material.SPLASH_POTION;
            case VEHICLE_ENTER -> Material.MINECART;
            case BUCKET_USAGE -> Material.BUCKET;
            case FROST_WALKER -> Material.ICE;
            case PISTON_MOVEMENT_ACROSS_CLAIM_BORDERS -> Material.PISTON;
            case INTERACTION_JUKEBOX -> Material.JUKEBOX;
            case INTERACTION_LEVERS -> Material.LEVER;
            case INTERACTION_BUTTONS -> Material.STONE_BUTTON;
            case INTERACTION_REPEATERS -> Material.REPEATER;
            case INTERACTION_COMPARATORS -> Material.COMPARATOR;
            case INTERACTION_DAYLIGHT_SENSORS -> Material.DAYLIGHT_DETECTOR;
            case INTERACTION_PRESSURE_PLATES -> Material.STONE_PRESSURE_PLATE;
            case INTERACTION_TRIPWIRE -> Material.TRIPWIRE_HOOK;
            case INTERACTION_TRAPDOORS -> Material.OAK_TRAPDOOR;
            case INTERACTION_DOORS -> Material.OAK_DOOR;
            case INTERACTION_FENCE_GATES -> Material.OAK_FENCE_GATE;
            case INTERACTION_FARMLAND -> Material.FARMLAND;
            case INTERACTION_CAMPFIRE -> Material.CAMPFIRE;
            case INTERACTION_BELLS -> Material.BELL;
            case INTERACTION_ATTACH_LEASH -> Material.LEAD;
            case INTERACTION_BONEMEAL -> Material.BONE_MEAL;
            case INTERACTION_BEEHIVES -> Material.BEE_NEST;
            case INTERACTION_NAME_TAGS -> Material.NAME_TAG;
            case INTERACTION_CANDLES -> Material.CANDLE;
            case INTERACTION_HANGING_ENTITIES -> Material.ITEM_FRAME;
            case INTERACTION_LIGHT_TNT, EXPLOSION_BLOCK_DAMAGE -> Material.TNT;
            case INTERACTION_ARMOR_STAND -> Material.ARMOR_STAND;
            case INTERACTION_TAME_ENTITY -> Material.BONE;
            case ENTITY_DAMAGE -> Material.IRON_SWORD;
            case EXCLUDE_PLAYERS -> Material.PLAYER_HEAD;
            case PROJECTILE_INTERACTIONS -> Material.ARROW;
            case FISHING -> Material.FISHING_ROD;
            case EXPLOSION_ENTITY_DAMAGE -> Material.GUNPOWDER;
            case ENTITY_MODIFICATIONS_WITHER -> Material.WITHER_SKELETON_SKULL;
            case ENTITY_MODIFICATIONS_ENDERMAN -> Material.ENDER_PEARL;
            case ENTITY_MODIFICATIONS_RAVAGER -> Material.SADDLE;
            default -> Material.BOOK;
        };
    }

}
