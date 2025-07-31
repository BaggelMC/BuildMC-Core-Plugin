package net.mathias2246.buildmc.status;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PlayerStatus implements Listener {

    public static final @NotNull NamespacedKey PLAYER_STATUS_PDC = Objects.requireNonNull(NamespacedKey.fromString("buildmc:player_status"));

    private static boolean hasStatus(@NotNull Player player) {
        return player.getPersistentDataContainer().has(PLAYER_STATUS_PDC);
    }

    public static boolean doesStatusExist(String status) {
        if (status == null) return false;

        return true;
    }

    public static void setPlayerStatus(@NotNull Player player, String status) {
        if (!doesStatusExist(status)) return;


        player.getPersistentDataContainer().set(
                PLAYER_STATUS_PDC,
                PersistentDataType.STRING,
                status
        );

    }

    public static void removePlayerStatus(@NotNull Player player) {
        player.getPersistentDataContainer().remove(PLAYER_STATUS_PDC);
    }

    @EventHandler
    public static void onPlayerJoin(PlayerJoinEvent event) {
        if (hasStatus(event.getPlayer())) {
            Player player = event.getPlayer();
            setPlayerStatus(
                    player,
                    player.getPersistentDataContainer().get(PLAYER_STATUS_PDC, PersistentDataType.STRING)
            );
        }
    }

}
