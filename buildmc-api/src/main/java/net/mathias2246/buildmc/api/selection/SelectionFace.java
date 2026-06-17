package net.mathias2246.buildmc.api.selection;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class SelectionFace {
    private final @NotNull BlockDisplay blockDisplay;

    private @NotNull Location origin;

    private final @NotNull BlockFace face;

    private boolean isDestroyed = false;

    public SelectionFace(@NotNull Location origin, @NotNull BlockData blockDisplay, @NotNull BlockFace face, @NotNull Vector2f size) {
        this.origin = origin;
        this.face = face;

        this.blockDisplay = origin.getWorld().spawn(
                origin,
                BlockDisplay.class,
                entity -> { // Applies logic before the entity is actually spawned
                    entity.setPersistent(false); // Displays should not be saved

                    entity.setVisibleByDefault(false);
                    entity.setBlock(blockDisplay);

                    entity.addScoreboardTag("selection_face");
                }
        );

        setTransform(size.x, size.y);
    }

    public void destroy() {
        blockDisplay.remove();
        isDestroyed = true;
    }

    public SelectionFace(@NotNull Location origin, @NotNull BlockData blockDisplay, @NotNull BlockFace face, float width, float height) {
        this.origin = origin;
        this.face = face;

        this.blockDisplay = origin.getWorld().spawn(
                origin,
                BlockDisplay.class,
                entity -> { // Applies logic before the entity is actually spawned
                    entity.setPersistent(false); // Displays should not be saved

                    entity.setVisibleByDefault(false);
                    entity.setBlock(blockDisplay);
                }
        );

        setTransform(width, height);
    }

    public @NotNull BlockFace getFace() {
        return face;
    }


    private void setTransform(
            float width,
            float height
    ) {
        switch (face) {
            case NORTH, SOUTH:
                blockDisplay.setTransformation(
                        new Transformation(
                                new Vector3f(),
                                new Quaternionf().identity(),
                                new Vector3f(width, height, 0.01f),
                                new Quaternionf().identity()
                        )
                );
                break;
            case WEST, EAST:
                blockDisplay.setTransformation(
                        new Transformation(
                                new Vector3f(),
                                new Quaternionf().identity(),
                                new Vector3f(0.01f, height, width),
                                new Quaternionf().identity()
                        )
                );
                break;
            case DOWN, UP:
                blockDisplay.setTransformation(
                        new Transformation(
                                new Vector3f(),
                                new Quaternionf().identity(),
                                new Vector3f(width, 0.01f, height),
                                new Quaternionf().identity()
                        )
                );
                break;
        }

    }

    private void setTransform(
            Vector3f scale
    ) {
        switch (face) {
            case NORTH:
                scale.z = 0.01f;
                blockDisplay.setTransformation(
                        new Transformation(
                                new Vector3f(),
                                new Quaternionf().identity(),
                                scale,
                                new Quaternionf().identity()
                        )
                );
                break;
            case SOUTH:
                scale.mul(-1);
                scale.z = 0.01f;
                blockDisplay.setTransformation(
                        new Transformation(
                                new Vector3f(),
                                new Quaternionf().identity(),
                                scale,
                                new Quaternionf().identity()
                        )
                );
                break;
            case WEST:
                scale.x = 0.01f;
                blockDisplay.setTransformation(
                        new Transformation(
                                new Vector3f(),
                                new Quaternionf().identity(),
                                scale,
                                new Quaternionf().identity()
                        )
                );
                break;
            case EAST:
                scale.mul(-1);
                scale.x = 0.01f;
                blockDisplay.setTransformation(
                        new Transformation(
                                new Vector3f(),
                                new Quaternionf().identity(),
                                scale,
                                new Quaternionf().identity()
                        )
                );
                break;
            case DOWN:
                scale.y = 0.01f;
                blockDisplay.setTransformation(
                        new Transformation(
                                new Vector3f(),
                                new Quaternionf().identity(),
                                scale,
                                new Quaternionf().identity()
                        )
                );
                break;
            case UP:
                scale.mul(-1);
                scale.y = 0.01f;
                blockDisplay.setTransformation(
                        new Transformation(
                                new Vector3f(),
                                new Quaternionf().identity(),
                                scale,
                                new Quaternionf().identity()
                        )
                );
                break;
        }

    }

    public @NotNull Location getOrigin() {
        return origin;
    }

    public void setOrigin(@NotNull Location newOrigin) {
        origin = newOrigin;

        blockDisplay.setTeleportDuration(0);
        blockDisplay.teleport(newOrigin);
    }

    public void setOrigin(@NotNull Location newOrigin, int teleportInterpolationDuration) {
        origin = newOrigin;

        blockDisplay.setTeleportDuration(teleportInterpolationDuration);
        blockDisplay.teleport(newOrigin);
    }

    void setInterpolationDuration(@IntRange(from=0, to=59) int interpolationDuration) {
        blockDisplay.setInterpolationDuration(interpolationDuration);
    }

    public void setBlock(@NotNull BlockData blockData) {
        blockDisplay.setBlock(blockData);
    }

    public void setBlock(@NotNull Material material) {
        blockDisplay.setBlock(
                material.createBlockData()
        );
    }

    public void setSize(float width, float height, int interpolationDelay, int interpolationDuration) {
        blockDisplay.setInterpolationDelay(interpolationDelay);
        blockDisplay.setInterpolationDuration(interpolationDuration);
        setSize(width, height);
    }

    public void setSize(float width, float height, int interpolationDuration) {
        blockDisplay.setInterpolationDelay(0);
        blockDisplay.setInterpolationDuration(interpolationDuration);
        setSize(width, height);
    }

    public void setSize(float width, float height) {
        setTransform(width, height);
    }

    public void setSize(Vector3f size) {
        setTransform(size);
    }

    public void showToPlayer(@NotNull Plugin plugin, @NotNull Player player) {
        player.showEntity(plugin, blockDisplay);
    }

    public void hideFromPlayer(@NotNull Plugin plugin, @NotNull Player player) {
        player.hideEntity(plugin, blockDisplay);
    }

    public boolean canPlayerSee(@NotNull Player player) {
        return player.canSee(blockDisplay);
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }
}


