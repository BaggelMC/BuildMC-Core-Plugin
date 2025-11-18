package net.mathias2246.buildmc.api.status;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface StatusManager {

    @SuppressWarnings("UnusedReturnValue")
    boolean removePlayerStatus(@NotNull Player player);

    void forceRemovePlayerStatus(@NotNull Player player);

    void setPlayerStatus(@NotNull Player player, String status, boolean join);

    void setPlayerName(@NotNull Player player, @Nullable Component component);

    void resetPlayerName(@NotNull Player player);
}
