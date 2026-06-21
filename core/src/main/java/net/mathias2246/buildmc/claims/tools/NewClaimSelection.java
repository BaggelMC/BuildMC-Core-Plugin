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

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static net.mathias2246.buildmc.CoreMain.plugin;

public class NewClaimSelection {
    private final @NotNull Player player;

    private Location lastMinCorner;
    private Location lastMaxCorner;

    public static int pointDistance = 4;

    private final @NotNull Set<Location> shownLocations = new HashSet<>();

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
        AtomicBoolean wasLastToLarge = new AtomicBoolean(false);

        lastMaxCorner = location.clone();
        lastMinCorner = location.clone();

        AtomicReference<BlockData> goldData = new AtomicReference<>(Material.GOLD_BLOCK.createBlockData());

        var world = location.getWorld();

        player.getScheduler().runAtFixedRate(plugin,
                task -> {
                    var updatedBounds = getBoundingBoxForDisplay();

                    if (updatedBounds == null) {
                        resetBlocks(player, shownLocations);   // <-- batch reset
                        shownLocations.clear();
                        task.cancel();
                        remove();
                        player.getPersistentDataContainer().remove(ClaimSelectionTool.SELECTION_EFFECT_KEY);
                        return;
                    }

                    // Always reset previously shown locations first (one batched packet set)
                    resetBlocks(player, shownLocations);


                    if (!ClaimCreate.doesHoldSelectionTool(player)) {
                        isShowing.set(false);
                        return;
                    } else if (!isShowing.get()) {
                        isShowing.set(true);
                    }

                    if (!wasLastToLarge.get() && (updatedBounds.getWidthX() / 16 > ClaimSelectionTool.maxSelectionSize || updatedBounds.getWidthZ() / 16 > ClaimSelectionTool.maxSelectionSize)) {
                        goldData.set(Material.REDSTONE_BLOCK.createBlockData());
                        wasLastToLarge.set(true);
                    } else if (wasLastToLarge.get()) {
                        goldData.set(Material.GOLD_BLOCK.createBlockData());
                        wasLastToLarge.set(false);
                    }

                    shownLocations.clear();

                    // --- Build all changes for this tick ---
                    List<BlockChange> changes = new ArrayList<>();

                    // X-axis border markers
                    for (int x = (int)updatedBounds.getMinX() + 1; x < updatedBounds.getMinX() + updatedBounds.getWidthX() - 1; x += pointDistance) {
                        Location locMin = location.clone();
                        locMin.setX(x);
                        locMin.setZ(updatedBounds.getMinZ());
                        locMin.setY(world.getHighestBlockYAt(x, (int)updatedBounds.getMinZ(), HeightMap.MOTION_BLOCKING_NO_LEAVES));
                        changes.add(new BlockChange(locMin, goldData.get()));
                        shownLocations.add(locMin.clone());

                        Location locMax = location.clone();
                        locMax.setX(x);
                        locMax.setZ(updatedBounds.getMaxZ() - 1);
                        locMax.setY(world.getHighestBlockYAt(x, (int)updatedBounds.getMaxZ() - 1, HeightMap.MOTION_BLOCKING_NO_LEAVES));
                        changes.add(new BlockChange(locMax, goldData.get()));
                        shownLocations.add(locMax.clone());
                    }

                    // Z-axis border markers
                    for (int z = (int)updatedBounds.getMinZ() + 1; z < updatedBounds.getMinZ() + updatedBounds.getWidthZ() - 1; z += pointDistance) {
                        Location locMin = location.clone();
                        locMin.setZ(z);
                        locMin.setX(updatedBounds.getMinX());
                        locMin.setY(world.getHighestBlockYAt((int)updatedBounds.getMinX(), z, HeightMap.MOTION_BLOCKING_NO_LEAVES));
                        changes.add(new BlockChange(locMin, goldData.get()));
                        shownLocations.add(locMin.clone());

                        Location locMax = location.clone();
                        locMax.setZ(z);
                        locMax.setX(updatedBounds.getMaxX() - 1);
                        locMax.setY(world.getHighestBlockYAt((int)updatedBounds.getMaxX() - 1, z, HeightMap.MOTION_BLOCKING_NO_LEAVES));
                        changes.add(new BlockChange(locMax, goldData.get()));
                        shownLocations.add(locMax.clone());
                    }

                    // Corners (redstone blocks)
                    BlockData redstone = Material.REDSTONE_BLOCK.createBlockData();

                    Location minCorner = lastMinCorner.clone();
                    minCorner.setX(updatedBounds.getMinX());
                    minCorner.setZ(updatedBounds.getMinZ());
                    minCorner.setY(world.getHighestBlockYAt((int)updatedBounds.getMinX(), (int)updatedBounds.getMinZ(), HeightMap.MOTION_BLOCKING_NO_LEAVES));
                    changes.add(new BlockChange(minCorner, redstone));
                    shownLocations.add(minCorner.clone());

                    // Update lastMinCorner for next tick
                    lastMinCorner.setX(minCorner.getX());
                    lastMinCorner.setY(minCorner.getY());
                    lastMinCorner.setZ(minCorner.getZ());

                    Location maxCorner = lastMaxCorner.clone();
                    maxCorner.setX(updatedBounds.getMaxX() - 1);
                    maxCorner.setZ(updatedBounds.getMaxZ() - 1);
                    maxCorner.setY(world.getHighestBlockYAt((int)updatedBounds.getMaxX() - 1, (int)updatedBounds.getMaxZ() - 1, HeightMap.MOTION_BLOCKING_NO_LEAVES));
                    changes.add(new BlockChange(maxCorner, redstone));
                    shownLocations.add(maxCorner.clone());

                    Location maxXCorner = lastMaxCorner.clone();
                    maxXCorner.setX(updatedBounds.getMaxX() - 2);
                    maxXCorner.setZ(updatedBounds.getMaxZ() - 1);
                    maxXCorner.setY(world.getHighestBlockYAt(maxXCorner.getBlockX(), maxXCorner.getBlockZ(), HeightMap.MOTION_BLOCKING_NO_LEAVES));
                    changes.add(new BlockChange(maxXCorner, goldData.get()));
                    shownLocations.add(maxXCorner.clone());

                    Location maxZCorner = lastMaxCorner.clone();
                    maxZCorner.setX(updatedBounds.getMaxX() - 1);
                    maxZCorner.setZ(updatedBounds.getMaxZ() - 2);
                    maxZCorner.setY(world.getHighestBlockYAt(maxZCorner.getBlockX(), maxZCorner.getBlockZ(), HeightMap.MOTION_BLOCKING_NO_LEAVES));
                    changes.add(new BlockChange(maxZCorner, goldData.get()));
                    shownLocations.add(maxZCorner.clone());

                    Location corner1 = location.clone();
                    corner1.setX(updatedBounds.getMinX());
                    corner1.setZ(updatedBounds.getMaxZ()-1);
                    corner1.setY(world.getHighestBlockYAt(corner1.getBlockX(), corner1.getBlockZ(), HeightMap.MOTION_BLOCKING_NO_LEAVES));
                    changes.add(new BlockChange(corner1, redstone));
                    shownLocations.add(corner1.clone());

                    Location corner2 = location.clone();
                    corner2.setX(updatedBounds.getMaxX()-1);
                    corner2.setZ(updatedBounds.getMinZ());
                    corner2.setY(world.getHighestBlockYAt(corner2.getBlockX(), corner2.getBlockZ(), HeightMap.MOTION_BLOCKING_NO_LEAVES));
                    changes.add(new BlockChange(corner2, redstone));
                    shownLocations.add(corner2.clone());

                    // Update lastMaxCorner for next tick
                    lastMaxCorner.setX(maxCorner.getX());
                    lastMaxCorner.setY(maxCorner.getY());
                    lastMaxCorner.setZ(maxCorner.getZ());

                    // Send all fake blocks in one batch
                    sendBatchedBlockChanges(player, changes);

                    // Timeout
                    if (counter.getAndIncrement() > 200) {
                        resetBlocks(player, shownLocations);
                        shownLocations.clear();
                        task.cancel();
                        remove();
                        player.getPersistentDataContainer().remove(ClaimSelectionTool.SELECTION_EFFECT_KEY);
                    }
                },
                () -> {
                    resetBlocks(player, shownLocations);
                    shownLocations.clear();
                    remove();
                }, // Destroy selection when player is gone
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

    // Simple record to pair a location with its block data
    private record BlockChange(Location loc, BlockData data) {}

    private void sendBatchedBlockChanges(Player player, List<BlockChange> changes) {
        for (BlockChange bc : changes) {
            player.sendBlockChange(bc.loc(), bc.data());
        }
    }

    private void resetBlocks(Player player, Collection<Location> locations) {
        for (Location loc : locations) {
            player.sendBlockChange(loc, loc.getBlock().getBlockData());
        }
    }
}
