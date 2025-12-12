package net.mathias2246.buildmc.claims.protections.misc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.api.item.ItemUtil;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.inventoryframework.gui.GuiItem;
import net.mathias2246.buildmc.inventoryframework.gui.type.util.Gui;
import net.mathias2246.buildmc.ui.UIUtil;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.Lectern;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class Containers extends Protection {

    public Containers(@Nullable ConfigurationSection section) {
        //noinspection SimplifiableConditionalExpression
        super(Objects.requireNonNull(NamespacedKey.fromString("buildmc:containers")), (section != null ? section.getBoolean("default", true) : true), section != null && section.getBoolean("is-hidden", false));
    }

    @Override
    public String getTranslationBaseKey() {
        return "claims.flags.container";
    }

    @Override
    public @NotNull GuiItem getDisplay(@NotNull Player uiHolder, @NotNull Gui gui) {
        String t = getTranslationBaseKey();

        ItemStack displayBase = new ItemStack(Material.CHEST);
        ItemUtil.editMeta(displayBase, (meta) -> {
            meta.setItemName(LegacyComponentSerializer.legacySection().serialize(
                    Message.msg(uiHolder, t+".name")
            ));
            meta.setLore(List.of(LegacyComponentSerializer.legacySection().serialize(Message.msg(uiHolder, t + ".lore")).split("\n")));
        });

        return new GuiItem(
                displayBase,
                UIUtil.noInteract
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onOpenContainer(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();

        Location loc = event.getInventory().getLocation();

        if (loc == null) return;

        Claim claim;
        try {
            claim = ClaimManager.getClaim(loc);
        } catch (SQLException e) {
            CoreMain.plugin.getLogger().severe("SQL error while getting claim: " + e);
            return;
        }
        if (claim == null) return;

        if (claim.getWhitelistedPlayers().contains(player.getUniqueId())) return;

        var invType = event.getInventory().getType();
        // Exceptions
        if (event.getInventory().getHolder() instanceof Lectern) return;
        if (invType.equals(InventoryType.ENDER_CHEST)) return;
        if (invType.equals(InventoryType.WORKBENCH)) return;
        if (invType.equals(InventoryType.ENCHANTING)) return;
        if (invType.equals(InventoryType.SMITHING)) return;
        if (invType.equals(InventoryType.LOOM)) return;
        if (invType.equals(InventoryType.CARTOGRAPHY)) return;
        if (invType.equals(InventoryType.GRINDSTONE)) return;
        if (invType.equals(InventoryType.STONECUTTER)) return;

        if (event.getInventory().getHolder() instanceof BlockState block) {

            if (!ClaimManager.isPlayerAllowed(player, getKey(), claim)) {
                CoreMain.plugin.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.container"));
                event.setCancelled(true);
            }

        } else if (event.getInventory().getHolder() instanceof Entity entity) {

            if (!ClaimManager.isPlayerAllowed(player, getKey(), claim)) {
                CoreMain.plugin.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.entity-container"));
                event.setCancelled(true);
            }

        } else {
            if (!ClaimManager.isPlayerAllowed(player, getKey(), claim)) {
                CoreMain.plugin.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.container"));
                event.setCancelled(true);
            }

        }
    }

    @EventHandler
    public void onLecternTakeBook(PlayerTakeLecternBookEvent event) {
        var block = event.getLectern();
        var player = event.getPlayer();

        Claim claim;
        try {
            claim = ClaimManager.getClaim(block.getLocation());
        } catch (SQLException e) {
            CoreMain.plugin.getLogger().severe("SQL error while getting claim: " + e);
            return;
        }
        if (claim == null) return;

        if (!ClaimManager.isPlayerAllowed(player, getKey(), claim)) {
            event.setCancelled(true);
            CoreMain.plugin.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.interact"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;

        var block = event.getClickedBlock();
        var player = event.getPlayer();
        Material type = block.getType();

        switch (event.getAction()) {
            case RIGHT_CLICK_BLOCK -> {}
            default -> { return; }
        }

        boolean isChiseledBookshelf = type == Material.CHISELED_BOOKSHELF;
        boolean isDecoratedPot = type == Material.DECORATED_POT;
        boolean isShelf = type.name().endsWith("_SHELF") || type.name().equals("SHELF");

        if (!isChiseledBookshelf && !isDecoratedPot && !isShelf)
            return;

        Claim claim;
        try {
            claim = ClaimManager.getClaim(block.getLocation());
        } catch (SQLException e) {
            CoreMain.plugin.getLogger().severe("SQL error while getting claim: " + e);
            return;
        }

        if (claim == null) return;

        if (claim.getWhitelistedPlayers().contains(player.getUniqueId())) return;

        if (!ClaimManager.isPlayerAllowed(player, getKey(), claim)) {
            event.setCancelled(true);
            CoreMain.plugin.sendPlayerActionBar(
                    player,
                    Component.translatable("messages.claims.not-accessible.container")
            );
        }
    }

}
