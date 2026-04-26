package net.mathias2246.buildmc.api.permission;

import org.bukkit.permissions.*;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PermissionGroup extends PermissibleBase {

    private final String name;
    private final Set<PermissionGroup> childGroups = new HashSet<>();
    private final Set<Permissible> members = new HashSet<>();

    public PermissionGroup(@NotNull String name, @NotNull Plugin plugin) {
        // PermissibleBase requires a ServerOperator
        // We pass a no-op operator since groups are never "ops"
        super(new ServerOperator() {
            @Override
            public boolean isOp() {
                return false;
            }

            @Override
            public void setOp(boolean b) {

            }
        });

        this.plugin = plugin;
        this.name = name;
    }

    public @NotNull String getName() {
        return name;
    }

    // --- Group membership ---

    public void addMember(@NotNull Permissible member) {
        members.add(member);
    }

    public void removeMember(@NotNull Permissible member) {
        members.remove(member);
    }

    public boolean hasMember(@NotNull Permissible member) {
        return hasMember(member, new HashSet<>());
    }

    private boolean hasMember(@NotNull Permissible member, @NotNull Set<PermissionGroup> visited) {
        if (!visited.add(this)) return false;
        if (members.contains(member)) return true;
        for (PermissionGroup child : childGroups) {
            if (child.hasMember(member, visited)) return true;
        }
        return false;
    }

    public @NotNull Set<Permissible> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    // --- Child group nesting ---

    public void addChildGroup(@NotNull PermissionGroup group) {
        if (group == this) throw new IllegalArgumentException("A group cannot contain itself!");
        if (group.wouldCreateCycle(this, new HashSet<>()))
            throw new IllegalArgumentException("Adding this group would create a circular reference!");
        childGroups.add(group);
        // Propagate the child group's permissions up into this group via attachment
        syncChildPermissions(group);
    }

    public void removeChildGroup(@NotNull PermissionGroup group) {
        childGroups.remove(group);
        recalculatePermissions();
    }

    public @NotNull Set<PermissionGroup> getChildGroups() {
        return Collections.unmodifiableSet(childGroups);
    }

    // --- Permission management ---

    private @Nullable PermissionAttachment attachment;
    private final Plugin plugin;

    /**
     * Grants a permission to this group and recalculates.
     */
    public void grantPermission(@NotNull String permission) {
        ensureAttachment();
        Objects.requireNonNull(attachment).setPermission(permission, true);
        recalculatePermissions();
    }

    /**
     * Revokes a permission from this group and recalculates.
     */
    public void revokePermission(@NotNull String permission) {
        ensureAttachment();
        Objects.requireNonNull(attachment).unsetPermission(permission);
        recalculatePermissions();
    }

    private void ensureAttachment() {
        if (attachment == null) {
            attachment = addAttachment(plugin);
        }
    }

    /**
     * Syncs a child group's effective permissions into this group's attachment.
     * Called when a child group is added or its permissions change.
     */
    private void syncChildPermissions(@NotNull PermissionGroup child) {
        ensureAttachment();
        for (PermissionAttachmentInfo entry : child.getEffectivePermissions()) {
            Objects.requireNonNull(attachment).setPermission(entry.getPermission(), entry.getValue());
        }
        recalculatePermissions();
    }

    /**
     * Fully recalculates permissions by rebuilding from scratch.
     * Call this whenever child group permissions change.
     */
    public void recalculate() {
        if (attachment != null) {
            removeAttachment(attachment);
            attachment = null;
        }
        ensureAttachment();
        // Re-add child group permissions
        for (PermissionGroup child : childGroups) {
            for (PermissionAttachmentInfo entry : child.getEffectivePermissions()) {
                attachment.setPermission(entry.getPermission(), entry.getValue());
            }
        }
        recalculatePermissions();
        // Propagate changes up to any parent groups that contain this group
        notifyParents();
    }

    // --- Cycle detection ---

    private boolean wouldCreateCycle(@NotNull PermissionGroup target, @NotNull Set<PermissionGroup> visited) {
        if (!visited.add(this)) return false;
        if (this == target) return true;
        for (PermissionGroup child : childGroups) {
            if (child.wouldCreateCycle(target, visited)) return true;
        }
        return false;
    }

    // --- Parent notification ---
    // Weak references to avoid memory leaks
    private final Set<PermissionGroup> parentGroups = Collections.newSetFromMap(new WeakHashMap<>());

    void registerParent(@NotNull PermissionGroup parent) {
        parentGroups.add(parent);
    }

    void unregisterParent(@NotNull PermissionGroup parent) {
        parentGroups.remove(parent);
    }

    private void notifyParents() {
        for (PermissionGroup parent : parentGroups) {
            parent.recalculate();
        }
    }

    // --- Applying to players ---

    /**
     * Applies this group's effective permissions to a player via a PermissionAttachment.
     * Store the returned attachment to remove it later on quit.
     */
    public @NotNull PermissionAttachment applyToPlayer(
            @NotNull org.bukkit.entity.Player player,
            @NotNull Plugin plugin) {
        PermissionAttachment playerAttachment = player.addAttachment(plugin);
        for (PermissionAttachmentInfo entry : getEffectivePermissions()) {
            playerAttachment.setPermission(entry.getPermission(), entry.getValue());
        }
        player.recalculatePermissions();
        return playerAttachment;
    }
}