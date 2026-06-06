package net.mathias2246.buildmc.player.permission;

import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;

public class PermissionManager implements net.mathias2246.buildmc.api.permission.PermissionManager {

    @Override
    public void addPermission(@NotNull Player player, @NotNull String permission, boolean persist) {

    }

    @Override
    public void removePermission(@NotNull Player player, @NotNull String permission) {

    }

    @Override
    public boolean hasPermission(@NotNull Player player, @NotNull String permission) {
        return false;
    }
}
