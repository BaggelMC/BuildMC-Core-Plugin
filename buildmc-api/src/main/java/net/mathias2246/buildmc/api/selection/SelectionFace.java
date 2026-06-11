package net.mathias2246.buildmc.api.selection;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class SelectionFace {
    private final @NotNull BlockDisplay blockDisplay;

    private @NotNull Location origin;

    public SelectionFace(@NotNull Location origin, @NotNull BlockData blockDisplay, @NotNull BlockFace face, @NotNull Vector2f size) {
        this.origin = origin;

        this.blockDisplay = origin.getWorld().spawn(
                origin,
                BlockDisplay.class,
                entity -> { // Applies logic before the entity is actually spawned
                    entity.setPersistent(false); // Displays should not be saved

                    entity.setVisibleByDefault(false);
                    entity.setBlock(blockDisplay);

                    Transformation transformation = new Transformation(
                            new Vector3f( // Center the block
                                    size.x / 2,
                                    size.y / 2,
                                    0
                            ),
                            blockFaceRotation(face),
                            new Vector3f(size.x, size.y, 0.01f),
                            new Quaternionf()
                    );
                    entity.setTransformation(transformation);
                }
        );
    }

    public @NotNull Location getOrigin() {
        return origin;
    }

    public void setOrigin(@NotNull Location origin) {
        this.origin = origin;
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
        Transformation transformation = blockDisplay.getTransformation();

        transformation.getScale().set(width, height, 0.01f);
        transformation.getTranslation().set(width / 2, height / 2, 0);

        blockDisplay.setTransformation(transformation);
    }

    public static Quaternionf blockFaceRotation(BlockFace face) {
        return switch (face) {
            case SOUTH -> new Quaternionf();
            case NORTH -> new Quaternionf().rotateY((float) Math.PI);
            case EAST  -> new Quaternionf().rotateY((float) -Math.PI / 2);
            case WEST  -> new Quaternionf().rotateY((float) Math.PI / 2);

            case UP    -> new Quaternionf().rotateX((float) -Math.PI / 2);
            case DOWN  -> new Quaternionf().rotateX((float) Math.PI / 2);

            default -> throw new IllegalArgumentException(
                    "Unsupported face: " + face);
        };
    }
}
