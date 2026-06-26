package net.mathias2246.buildmc.api.permission;

import org.bukkit.entity.Player;
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

    void addPermission(@NotNull Player player, @NotNull String permission, boolean persist);

    void removePermission(@NotNull Player player, @NotNull String permission);

    @Contract(pure = true)
    boolean hasPermission(@NotNull Player player, @NotNull String permission);
}
