package net.mathias2246.buildmc.claims.protections.misc;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.claims.protections.ProtectionUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Lectern;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public class Containers extends Protection {

    public Containers(@Nullable ConfigurationSection section) {
        //noinspection SimplifiableConditionalExpression
        super(Objects.requireNonNull(NamespacedKey.fromString("buildmc:containers")), (section != null ? section.getBoolean("default", true) : true), section != null && section.getBoolean("is-hidden", false));
    }

    @Override
    public @NonNull String getTranslationBaseKey() {
        return "claims.protections.container";
    }

    @Override
    public @NotNull ItemStack getDisplay(@NotNull Player uiHolder, @NotNull Gui gui) {
        String t = getTranslationBaseKey();

        return ProtectionUtil.createDisplayItem(uiHolder, Material.CHEST, t);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onOpenContainer(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();

        Location loc = event.getInventory().getLocation();

        if (loc == null) return;

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

        ProtectionUtil.handleProtection(event, this, loc, player);
    }

    @EventHandler
    public void onLecternTakeBook(PlayerTakeLecternBookEvent event) {
        var block = event.getLectern();
        var player = event.getPlayer();

        ProtectionUtil.handleProtection(event, this, block.getLocation(), player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;

        var block = event.getClickedBlock();
        var player = event.getPlayer();
        Material type = block.getType();

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        boolean isChiseledBookshelf = type == Material.CHISELED_BOOKSHELF;
        boolean isDecoratedPot = type == Material.DECORATED_POT;
        boolean isShelf = type.name().endsWith("_SHELF") || type.name().equals("SHELF");

        if (!isChiseledBookshelf && !isDecoratedPot && !isShelf)
            return;

        ProtectionUtil.handleProtection(event, this, block.getLocation(), player);
    }

}
