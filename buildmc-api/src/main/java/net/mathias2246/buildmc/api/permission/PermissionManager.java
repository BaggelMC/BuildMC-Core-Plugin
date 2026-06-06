package net.mathias2246.buildmc.api.permission;

import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/** Basic Permission handling system.
 *
 * <p>
 *      You can change if a permission should persist or not.
 * </p>
 *
 * **/
@ApiStatus.NonExtendable
public interface PermissionManager {

    public abstract void addPermission(@NotNull Player player, @NotNull String permission, boolean persist);

    public abstract void removePermission(@NotNull Player player, @NotNull String permission);

    @Contract(pure = true)
    public abstract boolean hasPermission(@NotNull Player player, @NotNull String permission);
}
