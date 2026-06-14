package net.mathias2246.buildmc.api.selection;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

public class BoundingBoxSelection {

    private @NotNull final SelectionFace[] faces = new SelectionFace[6];

    private @NotNull BoundingBox boundingBox;

    private @NotNull Location origin;
    private @NotNull Location oppositeOrigin;

    public BoundingBoxSelection(@NotNull BoundingBox boundingBox, @NotNull Location origin, @NotNull Material material) {
        this.boundingBox = boundingBox;
        this.origin = origin;
        this.oppositeOrigin = origin.clone();
        oppositeOrigin.add(boundingBox.getMax());

        faces[0] = new SelectionFace(origin, material.createBlockData(), BlockFace.NORTH, (float)boundingBox.getWidthX(), (float)boundingBox.getHeight());
        faces[1] = new SelectionFace(origin, material.createBlockData(), BlockFace.WEST, (float)boundingBox.getWidthZ(), (float)boundingBox.getHeight());
        faces[2] = new SelectionFace(origin, material.createBlockData(), BlockFace.DOWN, (float)boundingBox.getWidthX(), (float)boundingBox.getWidthZ());
        // Opposite Faces
        faces[3] = new SelectionFace(origin, material.createBlockData(), BlockFace.SOUTH, -(float)boundingBox.getWidthX(), -(float)boundingBox.getHeight());
        faces[4] = new SelectionFace(origin, material.createBlockData(), BlockFace.EAST, -(float)boundingBox.getWidthZ(), -(float)boundingBox.getHeight());
        faces[5] = new SelectionFace(origin, material.createBlockData(), BlockFace.UP, -(float)boundingBox.getWidthX(), -(float)boundingBox.getWidthZ());

    }

    public void showToPlayer(@NotNull Plugin plugin, @NotNull Player player) {
        for  (SelectionFace face : faces) {
            face.showToPlayer(plugin, player);
        }
    }
}
