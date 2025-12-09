package net.mathias2246.buildmc.permissions;

import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.permissions.PermissionGroup;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class PermissionGroupManager {

    private static final NamespacedKey GROUPS_KEY =
            new NamespacedKey(CoreMain.plugin, "permission_groups");

    private static final String DEFAULT_GROUP = "default";


    /**
     * Reads a player's assigned group IDs from PDC
     */
    public static @NotNull List<String> getAssignedGroups(@NotNull PersistentDataHolder holder) {
        String stored = holder.getPersistentDataContainer().get(GROUPS_KEY, PersistentDataType.STRING);

        List<String> groups;
        if (stored == null || stored.isEmpty()) {
            groups = new ArrayList<>();
        } else {
            groups = new ArrayList<>(Arrays.asList(stored.split(",")));
        }

        if (!groups.contains(DEFAULT_GROUP)) groups.add(DEFAULT_GROUP);

        return groups;
    }


    /**
     * Writes assigned group IDs back into PDC
     */
    private static void setAssignedGroups(
            @NotNull PersistentDataHolder holder,
            @NotNull Collection<String> groups
    ) {
        holder.getPersistentDataContainer().set(
                GROUPS_KEY,
                PersistentDataType.STRING,
                String.join(",", groups)
        );
    }

    /**
     * Assign a permission group to a player
     */
    public static void assignGroup(@NotNull PersistentDataHolder holder, @NotNull String groupId) {
        List<String> groups = new ArrayList<>(getAssignedGroups(holder));
        if (!groups.contains(groupId)) {
            groups.add(groupId);
            setAssignedGroups(holder, groups);
        }
    }

    /**
     * Remove a permission group from a player
     */
    public static void removeGroup(@NotNull PersistentDataHolder holder, @NotNull String groupId) {

        if (groupId.equalsIgnoreCase(DEFAULT_GROUP)) {
            return; // default group can never be removed
        }

        List<String> groups = new ArrayList<>(getAssignedGroups(holder));
        if (groups.remove(groupId)) {
            setAssignedGroups(holder, groups);
        }
    }


    /**
     * Returns the actual PermissionGroup objects.
     */
    public static @NotNull List<PermissionGroup> resolveGroups(@NotNull PersistentDataHolder holder) {
        List<String> ids = getAssignedGroups(holder);
        List<PermissionGroup> groups = new ArrayList<>(ids.size());

        for (String id : ids) {
            NamespacedKey key = idToNamespacedKey(id);
            if (key == null) continue;

            PermissionGroup group = CoreMain.permissionGroupRegistry.get(key);
            if (group != null) groups.add(group);
        }

        return groups;
    }


    /**
     * Recalculates permissions based on assigned groups.
     * <p>
     * 1) Groups are sorted by priority.
     * 2) Permissions from higher priority override lower ones.
     * 3) Applies permissions to the player
     */
    @SuppressWarnings("UnusedReturnValue")
    public static @NotNull Map<String, Boolean> recalculatePermissions(
            @NotNull PersistentDataHolder holder
    ) {
        List<PermissionGroup> groups = new ArrayList<>(resolveGroups(holder));

        PermissionGroup defaultGroup = CoreMain.permissionGroupRegistry.get(Objects.requireNonNull(idToNamespacedKey(DEFAULT_GROUP)));
        if (defaultGroup != null && groups.stream().noneMatch(g -> g.getId().equalsIgnoreCase(DEFAULT_GROUP))) {
            groups.add(defaultGroup);
        }

        // Sort by priority, lowest first
        groups.sort(Comparator.comparingInt(PermissionGroup::getPriority));

        Map<String, Boolean> result = new HashMap<>();

        for (PermissionGroup group : groups) {
            for (Map.Entry<String, Boolean> entry : group.getPermissions().entrySet()) {
                // Higher priority overrides lower since this loop goes low -> high
                //noinspection UseBulkOperation
                result.put(entry.getKey(), entry.getValue());
            }
        }

        if (holder instanceof Permissible permissible) {
            applyPermissions(permissible, result);
        }

        return result;
    }

    /**
     * Applies a recalculated permission map to a Bukkit permissible.
     * This clears previous calculated permissions.
     */
    private static void applyPermissions(
            @NotNull Permissible permissible,
            @NotNull Map<String, Boolean> permissions
    ) {
        permissible.getEffectivePermissions().forEach(info -> {
            if (info.getAttachment() != null) info.getAttachment().remove();
        });

        var attachment = permissible.addAttachment(CoreMain.plugin);

        for (Map.Entry<String, Boolean> perm : permissions.entrySet()) {
            attachment.setPermission(perm.getKey(), perm.getValue());
        }

        if (permissible instanceof Player player) {
            player.updateCommands();
        }
    }

    /**
     * Returns true if a PDC holder has a particular permission group.
     */
    public static boolean hasGroup(@NotNull PersistentDataHolder holder, @NotNull String id) {
        NamespacedKey key = NamespacedKey.fromString("buildmc:" + id.toLowerCase());
        if (key == null) return false;
        return CoreMain.permissionGroupRegistry.getOptional(key)
                .map(g -> getAssignedGroups(holder).contains(g.getId()))
                .orElse(false);
    }

    /**
     * Converts PermissionGroup ID to a NamespacedKey
     * <p>
     * Returns:
     * the created NamespacedKey. null if invalid
     */
    public static @Nullable NamespacedKey idToNamespacedKey(String id) {
        return NamespacedKey.fromString("buildmc:" + id.toLowerCase());
    }
}
