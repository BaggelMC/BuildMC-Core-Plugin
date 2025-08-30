package net.mathias2246.buildmc.api.item;

import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**An abstract-class that can be used to define custom-item types for your plugin.
 * Custom items are defined by some data that is stored inside the ItemStack PDC.
 * This doesn't actually define a new item type.*/
public abstract class AbstractCustomItem implements Keyed {

    private final @NotNull NamespacedKey key;

    private final @NotNull Plugin plugin;

    /**@return The plugin that owns this custom type*/
    public @NotNull Plugin getPlugin() {
        return plugin;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    private final @NotNull ItemStack defaultItemStack;

    /**The NamespacedKey used to store the id of the custom item inside the ItemStack PDC*/
    public static final @NotNull NamespacedKey CUSTOM_ITEM_PDC_KEY = Objects.requireNonNull(NamespacedKey.fromString("buildmc:custom_item"));

    public AbstractCustomItem(@NotNull Plugin plugin, @NotNull NamespacedKey key) {
        this.plugin = plugin;
        this.key = key;

        defaultItemStack = buildDefaultItemStack();
        if (defaultItemStack.getItemMeta() instanceof ItemMeta meta) {
            meta.getPersistentDataContainer().set(
                    CUSTOM_ITEM_PDC_KEY,
                    PersistentDataType.STRING,
                    key.toString()
            );
            defaultItemStack.setItemMeta(meta);
        }
    }

    /**Tries to read the custom item type from an ItemStack.
     * @return The NamespacedKey for the custom item type or null if not found or invalid*/
    public static @Nullable NamespacedKey getCustomItemKey(@Nullable ItemStack item) {
        if (item == null) return null;

        if (!(item.getItemMeta() instanceof ItemMeta meta)) return null;

        var s = meta.getPersistentDataContainer().get(CUSTOM_ITEM_PDC_KEY, PersistentDataType.STRING);
        if (s == null) return null;

        return NamespacedKey.fromString(s);
    }

    /**Gives a certain amount of this custom item to a player.*/
    public void giveToPlayer(@NotNull Player player, int amount) {
        var i = defaultItemStack.clone();
        i.setAmount(amount);
        player.getInventory().addItem(i);
    }

    /**Gives a single item of this type to a player.*/
    public void giveToPlayer(@NotNull Player player) {
        player.getInventory().addItem(defaultItemStack.clone());
    }

    /**Gives a single item of this type to a player and modifies the ItemMeta with a custom modifier.
     * @param modifier An implementation of a ItemMetaModifier to apply e.g. platform dependent modifications to the ItemMeta.*/
    public void giveToPlayer(@NotNull Player player, @NotNull ItemMetaModifier modifier) {
        var i = defaultItemStack.clone();
        if (i.getItemMeta() instanceof ItemMeta meta) {
            modifier.modifyMeta(meta, player);
            i.setItemMeta(meta);
        }
        player.getInventory().addItem(i);
    }

    /**
     * Checks if the given ItemStack is of this custom item type.
     * @return True if the given ItemStack is of this type.*/
    public boolean isThis(@Nullable ItemStack item) {
        if (item == null) return false;
        if (!(item.getItemMeta() instanceof ItemMeta meta)) return false;

        var pdc = meta.getPersistentDataContainer();
        return Objects.equals(pdc.get(CUSTOM_ITEM_PDC_KEY, PersistentDataType.STRING), key.toString());
    }

    /**Checks if the player can interact in any way with this custom item.
     * @implNote Should implement logic like, for example, cooldowns or location checks that should be applied.*/
    public abstract boolean canUse(@NotNull ItemStack item, @NotNull PlayerInteractEvent event);

    /**Called when a PlayerInteractEvent from the CustomItemListener was done with this custom item type*/
    @ApiStatus.Internal
    public void onInteractEvent(@NotNull ItemStack item, @NotNull PlayerInteractEvent event) {
        onInteract(item, event);
        var a = event.getAction();

        Location at;
        if (event.getClickedBlock() == null) at = event.getPlayer().getLocation();
        else at = event.getClickedBlock().getLocation();

        if (a.equals(Action.LEFT_CLICK_AIR) || a.equals(Action.LEFT_CLICK_BLOCK)) onLeftClick(item, at, event);
        else if (a.equals(Action.RIGHT_CLICK_AIR) || a.equals(Action.RIGHT_CLICK_BLOCK)) onRightClick(item, at, event);
    }

    /**Executed when a player left- or right-clicks with this custom-item type in his hand.*/
    protected abstract void onInteract(@NotNull ItemStack item, @NotNull PlayerInteractEvent event);

    /**Executed when a player left-clicks with this custom-item type in his hand.*/
    protected abstract void onLeftClick(@NotNull ItemStack item, @NotNull Location at, @NotNull PlayerInteractEvent event);
    /**Executed when a player right-clicks with this custom-item type in his hand.*/
    protected abstract void onRightClick(@NotNull ItemStack item, @NotNull Location at, @NotNull PlayerInteractEvent event);

    /**Creates the default ItemStack for this custom-item type
     * @implNote Should implement logic that changes e.g. ItemMeta like tools or consumables.
     * Is called inside the constructor to create the default ItemStack.*/
    protected abstract @NotNull ItemStack buildDefaultItemStack();

}
