package net.mathias2246.buildmc.tags;

import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**An implementation of a {@link Tag} storing {@link EntityType}s*/
public class EntityTypeTag implements org.bukkit.Tag<EntityType> {

    public EntityTypeTag(@NotNull NamespacedKey key, @NotNull Set<EntityType> EntityTypes) {
        this.key = key;
        this.EntityTypes = EntityTypes;
    }

    public EntityTypeTag(@NotNull Set<Tag<EntityType>> EntityTypeTags, @NotNull NamespacedKey key) {
        this.key = key;
        List<EntityType> m = new ArrayList<>();
        for (var b : EntityTypeTags) {
            m.addAll(b.getValues());
        }
        this.EntityTypes = Set.copyOf(m);
    }

    private final @NotNull NamespacedKey key;

    private final @NotNull Set<EntityType> EntityTypes;

    @Override
    public boolean isTagged(@NotNull EntityType EntityType) {
        return EntityTypes.contains(EntityType);
    }

    @Override
    public @NotNull Set<EntityType> getValues() {
        return EntityTypes;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }
}
