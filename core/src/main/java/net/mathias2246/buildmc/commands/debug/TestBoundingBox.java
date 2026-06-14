package net.mathias2246.buildmc.commands.debug;

import net.mathias2246.buildmc.api.selection.BoundingBoxSelection;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TestBoundingBox {

    public static @Nullable BoundingBoxSelection testSelection = null;

    public static int createTestBox(@NotNull Location origin, @NotNull BoundingBox boundingBox, @NotNull Material material) {
        testSelection = new BoundingBoxSelection(
                boundingBox,
                origin,
                material
        );

        return 1;
    }
}
