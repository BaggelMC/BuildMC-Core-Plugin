package net.mathias2246.buildmc.item.abstractTypes;

import net.mathias2246.buildmc.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**An abstract-class that can be used to create selection tools.
 * <p/>A selection tool that is made with this class will write the players selection into the players metadata.*/
public abstract class AbstractSelectionTool extends AbstractTool {

    /**The NamespacedKey used to identify the cooldown group of selection tools*/
    public static final @NotNull NamespacedKey SELECTION_TOOL_COOLDOWN_GROUP = Objects.requireNonNull(NamespacedKey.fromString("buildmc:selection_tool_cooldown"));

    public AbstractSelectionTool(@NotNull Plugin plugin, @NotNull NamespacedKey key) {
        super(plugin, key);
        firstSelectionKey = key + "_first_selection";
        secondSelectionKey = key + "_second_selection";
    }

    /**The metadata-key that is used to store the first selection.*/
    public final @NotNull String firstSelectionKey;
    /**The metadata-key that is used to store the second selection.*/
    public final @NotNull String secondSelectionKey;

    /**Checks is this custom item is usable.
     * <p>Should check for things like e.g. cooldown, or other conditions</p>*/
    public abstract boolean canUse(@NotNull ItemStack item, @NotNull PlayerInteractEvent event);

    @Override
    protected void onInteract(@NotNull ItemStack item, @NotNull PlayerInteractEvent event) {
        event.setCancelled(true);
    }

    @Override
    protected void onLeftClick(@NotNull ItemStack item, @NotNull Location at, @NotNull PlayerInteractEvent event) {
        if (!canUse(item, event)) return;
        if (!allowFirstSelection(item, at, event)) return;

        event.getPlayer().setMetadata(firstSelectionKey, new FixedMetadataValue(
                getPlugin(), LocationUtil.serialize(at)
        ));
        onSuccessfulFirstSelection(item, at, event);
    }

    /**Tries reading the first selection position from the player's metadata.
     * @return The Location of the first selection or null if not found or invalid*/
    public @Nullable Location getFirstSelection(@NotNull Player player) {
        var l = player.getMetadata(firstSelectionKey);
        if (l.isEmpty()) return null;
        return LocationUtil.tryDeserialize(l.getFirst().asString());
    }

    /**Tries reading the second selection position from the player's metadata.
     * @return The Location of the second selection or null if not found or invalid*/
    public @Nullable Location getSecondSelection(@NotNull Player player) {
        var l = player.getMetadata(secondSelectionKey);
        if (l.isEmpty()) return null;
        return LocationUtil.tryDeserialize(l.getFirst().asString());
    }

    @Override
    protected void onRightClick(@NotNull ItemStack item, @NotNull Location at, @NotNull PlayerInteractEvent event) {
        if (!canUse(item, event)) return;
        if (!allowSecondSelection(item, at, event)) return;

        event.getPlayer().setMetadata(secondSelectionKey, new FixedMetadataValue(
                getPlugin(), LocationUtil.serialize(at)
        ));
        onSuccessfulSecondSelection(item, at, event);
    }

    /**Checks if the player should be able to set the first selection position.
     *
     * <p>If this returns false, onSuccessfulFirstSelection will not be called.</p>*/
    public abstract boolean allowFirstSelection(@NotNull ItemStack item, @NotNull Location at, @NotNull PlayerInteractEvent event);
    /**Checks if the player should be able to set the second selection position.
     *
     * <p>If this returns false, onSuccessfulSecondSelection will not be called.</p>*/
    public abstract boolean allowSecondSelection(@NotNull ItemStack item, @NotNull Location at, @NotNull PlayerInteractEvent event);

    /**Called when the first selection was successful.*/
    public abstract void onSuccessfulFirstSelection(@NotNull ItemStack item, @NotNull Location at, @NotNull PlayerInteractEvent event);
    /**Called when the second selection was successful.*/
    public abstract void onSuccessfulSecondSelection(@NotNull ItemStack item, @NotNull Location at, @NotNull PlayerInteractEvent event);
}
