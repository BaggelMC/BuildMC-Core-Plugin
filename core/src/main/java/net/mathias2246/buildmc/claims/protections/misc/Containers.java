package net.mathias2246.buildmc.claims.protections.misc;

import io.papermc.paper.block.TileStateInventoryHolder;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.claims.protections.ProtectionConfig;
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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

@ProtectionConfig(value = "claims.protections.container", defaultEnabled = true)
public class Containers extends Protection {

    public Containers(@Nullable ConfigurationSection section) {
        //noinspection SimplifiableConditionalExpression
        super(Objects.requireNonNull(NamespacedKey.fromString("buildmc:containers")), (section != null ? section.getBoolean("default", true) : true), section != null && section.getBoolean("is-hidden", false));

        protectContainerBlocks = section == null ? true : section.getBoolean("protect-container-blocks", true);
        excludeVillagers = section == null ? true : section.getBoolean("exclude-villagers", true);
    }

    private final boolean protectContainerBlocks;
    private final boolean excludeVillagers;

    @Override
    public @NonNull String getTranslationBaseKey() {
        return "claims.protections.container";
    }

    @Override
    public @NotNull ItemStack getDisplay(@NotNull Player uiHolder) {
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
        switch (invType) {
            case InventoryType.ENDER_CHEST, InventoryType.WORKBENCH, InventoryType.ENCHANTING, InventoryType.SMITHING,
                 InventoryType.LOOM, InventoryType.CARTOGRAPHY, InventoryType.GRINDSTONE, InventoryType.STONECUTTER:
                return;
            case InventoryType.MERCHANT:
                if (excludeVillagers) return;
                break;
        }

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

        switch (type) {
            case Material.CHISELED_BOOKSHELF, Material.DECORATED_POT:
                break;
            default:
                if (type.name().endsWith("_SHELF") || type.name().equals("SHELF")) break;
                return;
        }

        ProtectionUtil.handleProtection(event, this, block.getLocation(), player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!protectContainerBlocks) return;

        var block = event.getBlock();
        var player = event.getPlayer();


        if (!(block.getState() instanceof TileStateInventoryHolder)) return;

        ProtectionUtil.handleProtection(event, this, block.getLocation(), player);
    }
}
