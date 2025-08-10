package net.mathias2246.buildmc.tags;

import org.bukkit.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MaterialTag implements org.bukkit.Tag<Material> {

    public MaterialTag(@NotNull NamespacedKey key, @NotNull Set<Material> materials) {
        this.key = key;
        this.materials = materials;
    }

    public MaterialTag(@NotNull Set<Tag<Material>> materialTags, @NotNull NamespacedKey key) {
        this.key = key;
        List<Material> m = new ArrayList<>();
        for (var b : materialTags) {
            m.addAll(b.getValues());
        }
        this.materials = Set.copyOf(m);
    }

    private final @NotNull NamespacedKey key;

    private final @NotNull Set<Material> materials;

    @Override
    public boolean isTagged(@NotNull Material material) {
        return materials.contains(material);
    }

    @Override
    public @NotNull Set<Material> getValues() {
        return materials;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }
}
