package net.mathias2246.buildmc.item;

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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class AbstractCustomItem implements Keyed {

    private final @NotNull NamespacedKey key;

    private final @NotNull Plugin plugin;

    public @NotNull Plugin getPlugin() {
        return plugin;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    private final @NotNull ItemStack defaultItemStack;

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


    public static @Nullable NamespacedKey getCustomItemKey(@Nullable ItemStack item) {
        if (item == null) return null;

        if (!(item.getItemMeta() instanceof ItemMeta meta)) return null;

        var s = meta.getPersistentDataContainer().get(CUSTOM_ITEM_PDC_KEY, PersistentDataType.STRING);
        if (s == null) return null;

        return NamespacedKey.fromString(s);
    }

    public void giveToPlayer(@NotNull Player player, int amount) {
        var i = defaultItemStack.clone();
        i.setAmount(amount);
        player.getInventory().addItem(i);
    }

    public void giveToPlayer(@NotNull Player player) {
        player.getInventory().addItem(defaultItemStack.clone());
    }

    public void giveToPlayer(@NotNull Player player, @NotNull ItemMetaModifier modifier) {
        var i = defaultItemStack.clone();
        if (i.getItemMeta() instanceof ItemMeta meta) {
            modifier.modifyMeta(meta, player);
            i.setItemMeta(meta);
        }
        player.getInventory().addItem(i);
    }

    public boolean isThis(@Nullable ItemStack item) {
        if (item == null) return false;
        if (!(item.getItemMeta() instanceof ItemMeta meta)) return false;

        var pdc = meta.getPersistentDataContainer();
        return Objects.equals(pdc.get(CUSTOM_ITEM_PDC_KEY, PersistentDataType.STRING), key.toString());
    }

    public abstract boolean canUse(@NotNull ItemStack item, @NotNull PlayerInteractEvent event);

    public void onInteractEvent(@NotNull ItemStack item, @NotNull PlayerInteractEvent event) {
        if (canUse(item, event)) {
            onInteract(item, event);
            var a = event.getAction();

            Location at;
            if (event.getClickedBlock() == null) at = event.getPlayer().getLocation();
            else at = event.getClickedBlock().getLocation();

            if (a.equals(Action.LEFT_CLICK_AIR) || a.equals(Action.LEFT_CLICK_BLOCK)) onLeftClick(item, at, event);
            else if (a.equals(Action.RIGHT_CLICK_AIR) || a.equals(Action.RIGHT_CLICK_BLOCK)) onRightClick(item, at, event);
        }
    }

    protected abstract void onInteract(@NotNull ItemStack item, @NotNull PlayerInteractEvent event);

    protected abstract void onLeftClick(@NotNull ItemStack item, @NotNull Location at, @NotNull PlayerInteractEvent event);
    protected abstract void onRightClick(@NotNull ItemStack item, @NotNull Location at, @NotNull PlayerInteractEvent event);

    protected abstract @NotNull ItemStack buildDefaultItemStack();



}
