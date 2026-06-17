package net.mathias2246.buildmc.claims.tools;

import net.mathias2246.buildmc.api.selection.BoundingBoxSelection;
import net.mathias2246.buildmc.commands.claim.ClaimCreate;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.BlockFace;
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

public class ClaimSelectionDisplay {

    private @Nullable BoundingBoxSelection selection;

    private final @NotNull Player player;

    public ClaimSelectionDisplay(@NotNull Player player) {
        this.player = player;
    }

    public void tryShow() {
        if (selection != null) {
            selection.destroy();
        }

        var bounds = getBoundingBoxForDisplay();
        if (bounds == null) return;

        selection = new BoundingBoxSelection(
                bounds,
                player.getWorld(),
                Material.GREEN_STAINED_GLASS
        );

        selection.showToPlayer(plugin, player);

        selection.setInterpolationDuration(0);

        AtomicBoolean isShowing = new AtomicBoolean(true);
        AtomicInteger counter = new AtomicInteger(0);

        player.getScheduler().runAtFixedRate(plugin,
                task -> {
                    var updatedBounds = getBoundingBoxForDisplay();

                    if (updatedBounds == null) {
                        task.cancel();
                        selection.destroy();
                        player.getPersistentDataContainer().remove(ClaimSelectionTool.SELECTION_EFFECT_KEY);
                        return;
                    }

                    if (!ClaimCreate.doesHoldSelectionTool(player)) {
                        selection.hideFromPlayer(plugin, player);
                        isShowing.set(false);
                        return;
                    } else if (!isShowing.get()) {
                        selection.showToPlayer(plugin, player);
                        isShowing.set(true);
                    }

                    if (updatedBounds.getWidthX() / 16 > ClaimSelectionTool.maxSelectionSize) {
                        selection.setFaceMaterial(BlockFace.NORTH, Material.RED_STAINED_GLASS);
                        selection.setFaceMaterial(BlockFace.SOUTH, Material.RED_STAINED_GLASS);
                    } else {
                        selection.setFaceMaterial(BlockFace.NORTH, Material.GREEN_STAINED_GLASS);
                        selection.setFaceMaterial(BlockFace.SOUTH, Material.GREEN_STAINED_GLASS);
                    }

                    if (updatedBounds.getWidthZ() / 16 > ClaimSelectionTool.maxSelectionSize) {
                        selection.setFaceMaterial(BlockFace.WEST, Material.RED_STAINED_GLASS);
                        selection.setFaceMaterial(BlockFace.EAST, Material.RED_STAINED_GLASS);
                    } else {
                        selection.setFaceMaterial(BlockFace.WEST, Material.GREEN_STAINED_GLASS);
                        selection.setFaceMaterial(BlockFace.EAST, Material.GREEN_STAINED_GLASS);
                    }

                    selection.setBoundingBox(updatedBounds);

                    // Timeout for selection display
                    if (counter.getAndIncrement() > 200) {
                        task.cancel();
                        selection.destroy();
                        player.getPersistentDataContainer().remove(ClaimSelectionTool.SELECTION_EFFECT_KEY);

                    }
                },
                () -> selection.destroy(), // Destroy selection when player is gone
                1,
                10
                );
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

        second.setY(player.getLocation().getY()+64);
        first.setY(player.getLocation().getY()-32);

        return new BoundingBox(
                (Math.floor((double)Math.min(first.getBlockX(), second.getBlockX()) / 16)) * 16,
                (Math.floor((double)Math.min(first.getBlockY(), second.getBlockY()) / 16)) * 16,
                (Math.floor((double)Math.min(first.getBlockZ(), second.getBlockZ()) / 16)) * 16,
                (Math.ceil((double)Math.max(first.getBlockX(), second.getBlockX()) / 16)) * 16,
                (Math.ceil((double)Math.max(first.getBlockY(), second.getBlockY()) / 16)) * 16,
                (Math.ceil((double)Math.max(first.getBlockZ(), second.getBlockZ()) / 16)) * 16
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
