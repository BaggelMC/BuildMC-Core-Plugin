package net.mathias2246.buildmc.player.status;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.event.player.StatusChangeEvent;
import net.mathias2246.buildmc.api.status.StatusInstance;
import net.mathias2246.buildmc.api.status.StatusManager;
import net.mathias2246.buildmc.status.PlayerStatusUtil;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static net.mathias2246.buildmc.status.PlayerStatusUtil.PLAYER_STATUS_PDC;

public class PlayerStatus implements Listener, StatusManager {

    public void setPlayerStatus(@NotNull Player player, String status, boolean join) {
        PlayerStatusUtil.setPlayerStatus(player, status, join);
    }

    @Override
    public void setPlayerName(@NotNull Player player, @Nullable Component component) {
        player.playerListName(component);
        player.displayName(component);
        player.customName(component);
        player.setCustomNameVisible(true);
    }

    @Override
    public void resetPlayerName(@NotNull Player player) {
        player.playerListName(null);
        player.displayName(null);
        player.customName(null);
        player.setCustomNameVisible(true);
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean removePlayerStatus(@NotNull Player player) {
        @Nullable StatusInstance old = CoreMain.statusesRegistry.get(Objects.requireNonNull(NamespacedKey.fromString("buildmc:" + player.getPersistentDataContainer().get(PLAYER_STATUS_PDC, PersistentDataType.STRING))).key());

        StatusChangeEvent e = new StatusChangeEvent(player, old, null);
        Bukkit.getPluginManager().callEvent(e);
        if (e.isCancelled()) {
            return false;
        }

       forceRemovePlayerStatus(player);

        return true;
    }

    public void forceRemovePlayerStatus(@NotNull Player player) {
        player.getPersistentDataContainer().remove(PLAYER_STATUS_PDC);

        player.playerListName(null);
        player.displayName(null);
        player.customName(null);
        player.setCustomNameVisible(false);
    }
}