package net.mathias2246.buildmc.api.permissions;

import net.mathias2246.buildmc.util.Group;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public final class PermissionGroup implements Group {

    private final @NotNull NamespacedKey key;
    private final @NotNull String id;
    private final int priority;
    private final @NotNull Map<String, Boolean> permissions;

    public PermissionGroup(
            @NotNull NamespacedKey key,
            @NotNull String id,
            int priority,
            @NotNull Map<String, Boolean> permissions
    ) {
        this.key = Objects.requireNonNull(key, "key");
        this.id = Objects.requireNonNull(id, "id");
        this.priority = priority;

        this.permissions = Collections.unmodifiableMap(
                Objects.requireNonNull(permissions, "permissions")
        );
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public @NotNull Map<String, Boolean> getPermissions() {
        return permissions;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }
}
