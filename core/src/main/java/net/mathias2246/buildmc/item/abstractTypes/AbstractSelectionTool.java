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

public abstract class AbstractSelectionTool extends AbstractTool {

    public static final @NotNull NamespacedKey SELECTION_TOOL_COOLDOWN_GROUP = Objects.requireNonNull(NamespacedKey.fromString("buildmc:selection_tool_cooldown"));

    public AbstractSelectionTool(@NotNull Plugin plugin, @NotNull NamespacedKey key) {
        super(plugin, key);
        firstSelectionKey = key + "_first_selection";
        secondSelectionKey = key + "_second_selection";
    }


    public final @NotNull String firstSelectionKey;
    public final @NotNull String secondSelectionKey;

    public abstract boolean canUse(@NotNull ItemStack item, @NotNull PlayerInteractEvent event);

    @Override
    protected void onInteract(@NotNull ItemStack item, @NotNull PlayerInteractEvent event) {
        event.setCancelled(true);
    }

    @Override
    protected void onLeftClick(@NotNull ItemStack item, @NotNull Location at, @NotNull PlayerInteractEvent event) {
        if (!allowFirstSelection(item, at, event)) return;

        event.getPlayer().setMetadata(firstSelectionKey, new FixedMetadataValue(
                getPlugin(), LocationUtil.serialize(at)
        ));
        onSuccessfulFirstSelection(item, at, event);
    }

    public @Nullable Location getFirstSelection(@NotNull Player player) {
        return LocationUtil.tryDeserialize(player.getMetadata(firstSelectionKey).getFirst().asString());
    }

    public @Nullable Location getSecondSelection(@NotNull Player player) {
        return LocationUtil.tryDeserialize(player.getMetadata(secondSelectionKey).getFirst().asString());
    }

    @Override
    protected void onRightClick(@NotNull ItemStack item, @NotNull Location at, @NotNull PlayerInteractEvent event) {

        if (!allowSecondSelection(item, at, event)) return;

        event.getPlayer().setMetadata(secondSelectionKey, new FixedMetadataValue(
                getPlugin(), LocationUtil.serialize(at)
        ));
        onSuccessfulSecondSelection(item, at, event);
    }

    public abstract boolean allowFirstSelection(@NotNull ItemStack item, @NotNull Location at, @NotNull PlayerInteractEvent event);
    public abstract boolean allowSecondSelection(@NotNull ItemStack item, @NotNull Location at, @NotNull PlayerInteractEvent event);

    public abstract void onSuccessfulFirstSelection(@NotNull ItemStack item, @NotNull Location at, @NotNull PlayerInteractEvent event);
    public abstract void onSuccessfulSecondSelection(@NotNull ItemStack item, @NotNull Location at, @NotNull PlayerInteractEvent event);
}
