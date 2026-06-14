package net.mathias2246.buildmc.api.selection;

import org.bukkit.block.BlockFace;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

class SelectionFacing {
    record FaceOrientation(
            Quaternionf rotation,
            Vector3f widthAxis,
            Vector3f heightAxis,
            Vector3f normal
    ) {}

    static FaceOrientation orientation(BlockFace face) {
        return switch (face) {
            case SOUTH -> new FaceOrientation(
                    new Quaternionf(),
                    new Vector3f(1, 0, 0),
                    new Vector3f(0, 1, 0),
                    new Vector3f(0, 0, 1)
            );

            case NORTH -> new FaceOrientation(
                    new Quaternionf().rotateY((float) Math.PI),
                    new Vector3f(-1, 0, 0),
                    new Vector3f(0, 1, 0),
                    new Vector3f(0, 0, -1)
            );

            case EAST -> new FaceOrientation(
                    new Quaternionf().rotateY((float) Math.PI / 2),
                    new Vector3f(0, 0, -1),
                    new Vector3f(0, 1, 0),
                    new Vector3f(1, 0, 0)
            );

            case WEST -> new FaceOrientation(
                    new Quaternionf().rotateY(-(float) Math.PI / 2),
                    new Vector3f(0, 0, 1),
                    new Vector3f(0, 1, 0),
                    new Vector3f(-1, 0, 0)
            );

            case UP -> new FaceOrientation(
                    new Quaternionf().rotateX(-(float) Math.PI / 2),
                    new Vector3f(1, 0, 0),
                    new Vector3f(0, 0, 1),
                    new Vector3f(0, 1, 0)
            );

            case DOWN -> new FaceOrientation(
                    new Quaternionf().rotateX((float) Math.PI / 2),
                    new Vector3f(1, 0, 0),
                    new Vector3f(0, 0, -1),
                    new Vector3f(0, -1, 0)
            );

            default -> throw new IllegalArgumentException(face.toString());
        };
    }

    static Transformation createTransformation(
            BlockFace face,
            float width,
            float height
    ) {
        FaceOrientation orientation = orientation(face);

        Vector3f translation = new Vector3f(
                orientation.widthAxis()
                        .mul(width / 2f, new Vector3f())
                        .add(
                                orientation.heightAxis()
                                        .mul(height / 2f, new Vector3f())
                        )
        );

        return new Transformation(
                translation,
                orientation.rotation(),
                new Vector3f(width, height, 0.01f),
                new Quaternionf()
        );
    }
}
