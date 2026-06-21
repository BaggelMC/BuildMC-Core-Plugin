package net.mathias2246.buildmc.claims.tools;

import net.mathias2246.buildmc.commands.claim.ClaimCreate;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static net.mathias2246.buildmc.CoreMain.plugin;

public class ClaimSelectionDisplayAlt {

    private @Nullable BlockDisplay displayA;
    private @Nullable BlockDisplay displayB;

    private final @NotNull Player player;

    public ClaimSelectionDisplayAlt(@NotNull Player player) {
        this.player = player;
    }

    public void tryShow() {
        if (displayA != null && displayB != null) {
           displayA.remove();
           displayB.remove();
        }

        var bounds = getBoundingBoxForDisplay();
        if (bounds == null) return;

        Location location = player.getLocation().clone();
        location.setRotation(0, 0);
        location.set(bounds.getMinX(), bounds.getMinY(), bounds.getMinZ());

        displayA = player.getWorld().spawn(
                location,
                BlockDisplay.class,
                entity -> { // Applies logic before the entity is actually spawned
                    entity.setPersistent(false); // Displays should not be saved

                    entity.setVisibleByDefault(false);
                    entity.setBlock(Material.GREEN_STAINED_GLASS.createBlockData());

                    entity.addScoreboardTag("selection_face");
                }
        );

        displayB = player.getWorld().spawn(
                location,
                BlockDisplay.class,
                entity -> { // Applies logic before the entity is actually spawned
                    entity.setPersistent(false); // Displays should not be saved

                    entity.setVisibleByDefault(false);
                    entity.setBlock(Material.GREEN_STAINED_GLASS.createBlockData());

                    entity.addScoreboardTag("selection_face");
                }
        );

        player.showEntity(plugin, displayA);
        player.showEntity(plugin, displayB);

        AtomicBoolean isShowing = new AtomicBoolean(true);
        AtomicInteger counter = new AtomicInteger(0);

        displayA.setTeleportDuration(0);
        displayB.setTeleportDuration(0);

        displayA.setInterpolationDuration(0);
        displayB.setInterpolationDuration(0);

        displayA.setBrightness(
                new Display.Brightness(
                        15, 0
                )
        );

        displayB.setBrightness(
                new Display.Brightness(
                        15, 0
                )
        );

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
                        player.hideEntity(plugin, displayA);
                        player.hideEntity(plugin, displayB);
                        isShowing.set(false);
                        return;
                    } else if (!isShowing.get()) {
                        player.showEntity(plugin, displayA);
                        player.showEntity(plugin, displayB);
                        isShowing.set(true);
                    }

                    if (updatedBounds.getWidthX() / 16 > ClaimSelectionTool.maxSelectionSize) {
                        displayA.setBlock(Material.RED_STAINED_GLASS.createBlockData());
                        displayB.setBlock(Material.RED_STAINED_GLASS.createBlockData());
                    } else {
                        displayA.setBlock(Material.GREEN_STAINED_GLASS.createBlockData());
                        displayB.setBlock(Material.GREEN_STAINED_GLASS.createBlockData());
                    }

                    if (updatedBounds.getWidthZ() / 16 > ClaimSelectionTool.maxSelectionSize) {
                        displayA.setBlock(Material.RED_STAINED_GLASS.createBlockData());
                        displayB.setBlock(Material.RED_STAINED_GLASS.createBlockData());
                    } else {
                        displayA.setBlock(Material.GREEN_STAINED_GLASS.createBlockData());
                        displayB.setBlock(Material.GREEN_STAINED_GLASS.createBlockData());
                    }

                    location.set(updatedBounds.getMinX(), updatedBounds.getMinY(), updatedBounds.getMinZ());


                    displayA.teleport(location);

                    displayB.teleport(location);

                    displayA.setTransformation(
                            new Transformation(
                                    new Vector3f(
                                            0,
                                            0,
                                            0
                                    ),
                                    new Quaternionf().identity(),
                                    new Vector3f(
                                            (float) updatedBounds.getWidthX(),
                                            (float) updatedBounds.getHeight(),
                                            (float) updatedBounds.getWidthZ()
                                    ),
                                    new Quaternionf()
                            )
                    );

                    displayB.setTransformation(
                            new Transformation(
                                    new Vector3f(
                                            (float) updatedBounds.getWidthX(),
                                            (float) updatedBounds.getHeight(),
                                            (float) updatedBounds.getWidthZ()
                                    ),
                                    new Quaternionf().identity(),
                                    new Vector3f(
                                            (float) -updatedBounds.getWidthX(),
                                            (float) -updatedBounds.getHeight(),
                                            (float) -updatedBounds.getWidthZ()
                                    ),
                                    new Quaternionf()
                            )
                    );

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
        if (displayA != null) {
            displayA.remove();
            displayA = null;
        }
        if (displayB!= null) {
            displayB.remove();
            displayA = null;
        }
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
