package net.mathias2246.buildmc.api.selection;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BoundingBoxSelection {

    private final SelectionFace[] faces = new SelectionFace[6];

    private @NotNull BoundingBox boundingBox;

    private final @NotNull Location origin;
    private final @NotNull Location oppositeOrigin;

    private int interpolationDuration = 0;

    public BoundingBoxSelection(@NotNull BoundingBox boundingBox, @NotNull World world, @NotNull Material material) {
        this.boundingBox = boundingBox;
        this.origin = new Location(world, boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ());

        this.oppositeOrigin = origin.clone();
        oppositeOrigin.add(boundingBox.getMax());

        faces[0] = new SelectionFace(origin, material.createBlockData(), BlockFace.NORTH, (float)boundingBox.getWidthX(), (float)boundingBox.getHeight());
        faces[1] = new SelectionFace(origin, material.createBlockData(), BlockFace.WEST, (float)boundingBox.getWidthZ(), (float)boundingBox.getHeight());
        faces[2] = new SelectionFace(origin, material.createBlockData(), BlockFace.DOWN, (float)boundingBox.getWidthX(), (float)boundingBox.getWidthZ());
        // Opposite Faces
        faces[3] = new SelectionFace(oppositeOrigin, material.createBlockData(), BlockFace.SOUTH, -(float)boundingBox.getWidthX(), -(float)boundingBox.getHeight());
        faces[4] = new SelectionFace(oppositeOrigin, material.createBlockData(), BlockFace.EAST, -(float)boundingBox.getWidthZ(), -(float)boundingBox.getHeight());
        faces[5] = new SelectionFace(oppositeOrigin, material.createBlockData(), BlockFace.UP, -(float)boundingBox.getWidthX(), -(float)boundingBox.getWidthZ());

    }

    public void setFaceMaterial(@NotNull BlockFace face, @NotNull Material material) {
        int index = switch (face) {
            case BlockFace.NORTH -> 0;
            case BlockFace.WEST -> 1;
            case BlockFace.DOWN -> 2;
            case BlockFace.SOUTH -> 3;
            case BlockFace.EAST -> 4;
            case BlockFace.UP -> 5;
            default -> throw new IllegalArgumentException("Invalid face " + face);
        };

        var selectionFace = faces[index];
        if (selectionFace == null) return;

        selectionFace.setBlock(material);
    }

    public void setBoundingBox(@NotNull BoundingBox boundingBox) {
        this.boundingBox = boundingBox;

        origin.set(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ());

        oppositeOrigin.set(boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());

        for (SelectionFace face : faces) {
            updateFace(face);
        }
    }

    private void updateFace(@Nullable SelectionFace selectionFace) {
        if (selectionFace == null) return;

        setInterpolationDuration(interpolationDuration);

        switch (selectionFace.getFace()) {
            case BlockFace.DOWN, BlockFace.NORTH, BlockFace.WEST:
                selectionFace.setOrigin(origin, interpolationDuration);
                break;
            case BlockFace.UP, BlockFace.SOUTH, BlockFace.EAST:
                selectionFace.setOrigin(oppositeOrigin, interpolationDuration);
                break;
        }

        selectionFace.setSize(boundingBox.getMax().subtract(boundingBox.getMin()).toVector3f());
    }

    public void destroy() {
        for (SelectionFace face : faces) {
            face.destroy();
        }
    }

    public void showToPlayer(@NotNull Plugin plugin, @NotNull Player player) {
        for (SelectionFace face : faces) {
            if (face == null) continue;

            face.showToPlayer(plugin, player);
        }
    }

    public void hideFromPlayer(@NotNull Plugin plugin, @NotNull Player player) {
        for (SelectionFace face : faces) {
            if (face == null) continue;

            face.hideFromPlayer(plugin, player);
        }
    }

    public int getInterpolationDuration() {
        return interpolationDuration;
    }

    public void setInterpolationDuration(@IntRange(from=0, to=59) int interpolationDuration) {
        this.interpolationDuration = Math.clamp(interpolationDuration, 0, 59);

    }
}
