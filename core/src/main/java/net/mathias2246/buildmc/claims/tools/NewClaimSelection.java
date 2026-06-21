package net.mathias2246.buildmc.claims.tools;

import net.mathias2246.buildmc.commands.claim.ClaimCreate;
import org.bukkit.FluidCollisionMode;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static net.mathias2246.buildmc.CoreMain.plugin;

public class NewClaimSelection {
    private final @NotNull Player player;

    private Location lastMinCorner;
    private Location lastMaxCorner;

    public NewClaimSelection(@NotNull Player player) {
        this.player = player;
    }

    public void tryShow() {

        var bounds = getBoundingBoxForDisplay();
        if (bounds == null) return;

        Location location = player.getLocation().clone();
        location.setRotation(0, 0);
        location.set(bounds.getMinX(), bounds.getMinY(), bounds.getMinZ());

        AtomicBoolean isShowing = new AtomicBoolean(true);
        AtomicInteger counter = new AtomicInteger(0);

        lastMaxCorner = location.clone();
        lastMinCorner = location.clone();

        BlockData goldData = Material.GOLD_BLOCK.createBlockData();

        player.getScheduler().runAtFixedRate(plugin,
                task -> {
                    var updatedBounds = getBoundingBoxForDisplay();

                    if (updatedBounds == null) {
                        task.cancel();
                        remove();
                        player.getPersistentDataContainer().remove(ClaimSelectionTool.SELECTION_EFFECT_KEY);
                        return;
                    }

                    if (!ClaimCreate.doesHoldSelectionTool(player)) {
                        player.sendBlockChange(lastMinCorner, lastMinCorner.getBlock().getBlockData());
                        player.sendBlockChange(lastMaxCorner, lastMaxCorner.getBlock().getBlockData());
                        isShowing.set(false);
                        return;
                    } else if (!isShowing.get()) {
                        isShowing.set(true);
                    }

                    player.sendBlockChange(lastMinCorner, lastMinCorner.getBlock().getBlockData());
                    player.sendBlockChange(lastMaxCorner, lastMaxCorner.getBlock().getBlockData());

                    for (int x = (int)updatedBounds.getMinX() + 1; x < updatedBounds.getMinX() + updatedBounds.getWidthX() - 1; x += 8) {
                        location.setZ(bounds.getMinZ());
                        location.setX(x);
                        location.setY(lastMinCorner.getWorld().getHighestBlockYAt(location.getBlockX(), location.getBlockZ(), HeightMap.MOTION_BLOCKING_NO_LEAVES));

                        player.sendBlockChange(location, goldData);

                        location.setZ(bounds.getMaxZ() - 1);
                        location.setY(lastMinCorner.getWorld().getHighestBlockYAt(location.getBlockX(), location.getBlockZ(), HeightMap.MOTION_BLOCKING_NO_LEAVES));

                        player.sendBlockChange(location, goldData);
                    }

                    for (int z = (int)updatedBounds.getMinZ() + 1; z < updatedBounds.getMinZ() + updatedBounds.getWidthZ() - 1; z += 8) {
                        location.setX(bounds.getMinX());
                        location.setZ(z);
                        location.setY(lastMinCorner.getWorld().getHighestBlockYAt(location.getBlockX(), location.getBlockZ(), HeightMap.MOTION_BLOCKING_NO_LEAVES));

                        player.sendBlockChange(location, goldData);

                        location.setX(bounds.getMaxX() - 1);
                        location.setY(lastMinCorner.getWorld().getHighestBlockYAt(location.getBlockX(), location.getBlockZ(), HeightMap.MOTION_BLOCKING_NO_LEAVES));

                        player.sendBlockChange(location, goldData);
                    }

                    lastMinCorner.setX(updatedBounds.getMinX());
                    lastMinCorner.setZ(updatedBounds.getMinZ());
                    lastMinCorner.setY(lastMinCorner.getWorld().getHighestBlockYAt((int)updatedBounds.getMinX(), (int)updatedBounds.getMinZ(), HeightMap.MOTION_BLOCKING_NO_LEAVES));
                    player.sendBlockChange(lastMinCorner, Material.REDSTONE_BLOCK.createBlockData());

                    lastMaxCorner.setX(updatedBounds.getMaxX() - 1);
                    lastMaxCorner.setZ(updatedBounds.getMaxZ() - 1);
                    lastMaxCorner.setY(lastMaxCorner.getWorld().getHighestBlockYAt((int)updatedBounds.getMaxX() - 1 , (int)updatedBounds.getMaxZ() - 1, HeightMap.MOTION_BLOCKING_NO_LEAVES));
                    player.sendBlockChange(lastMaxCorner, Material.REDSTONE_BLOCK.createBlockData());

                    // Timeout for selection display
                    if (counter.getAndIncrement() > 200) {
                        task.cancel();
                        remove();
                        player.getPersistentDataContainer().remove(ClaimSelectionTool.SELECTION_EFFECT_KEY);

                    }
                },
                this::remove, // Destroy selection when player is gone
                1,
                10
        );
    }

    public void remove() {
        player.sendBlockChange(lastMinCorner, lastMinCorner.getBlock().getBlockData());
        player.sendBlockChange(lastMaxCorner, lastMaxCorner.getBlock().getBlockData());
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    @Contract(pure = true)
    public @Nullable BoundingBox getBoundingBoxForDisplay() {
        @Nullable Location first = ClaimCreate.getFirstSelection(player);
        @Nullable Location second = ClaimCreate.getSecondSelection(player);

        if (first == null && second == null) return null;

        first = getLocation(first);

        second = getLocation(second);

        if (!first.getWorld().equals(second.getWorld())) return null;

        second.setY(player.getLocation().getY()+32);
        first.setY(player.getLocation().getY()-32);

        return new BoundingBox(
                (Math.floor((double)Math.min(first.getBlockX(), second.getBlockX()) / 16)) * 16,
                first.getBlockY(),
                (Math.floor((double)Math.min(first.getBlockZ(), second.getBlockZ()) / 16)) * 16,
                (Math.floor((double)Math.max(first.getBlockX(), second.getBlockX()) / 16) + 1) * 16,
                second.getBlockY(),
                (Math.floor((double)Math.max(first.getBlockZ(), second.getBlockZ()) / 16) + 1) * 16
        );
    }

    @NonNull
    private Location getLocation(@Nullable Location loc) {
        if (loc == null) {
            var ray = player.rayTraceBlocks(Objects.requireNonNull(player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE)).getValue(), FluidCollisionMode.NEVER);
            if (ray == null) {
                loc = player.getLocation();
            } else {
                var targetPos = ray.getHitPosition();

                loc = new Location(
                        player.getWorld(),
                        targetPos.getBlockX(),
                        targetPos.getBlockY(),
                        targetPos.getBlockZ()
                );
            }
        }
        return loc;
    }
}
