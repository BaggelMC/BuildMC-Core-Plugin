package net.mathias2246.buildmc.status;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.mathias2246.buildmc.util.Sounds;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static net.mathias2246.buildmc.Main.audiences;

public class PlayerStatus implements Listener {

    public static final @NotNull NamespacedKey PLAYER_STATUS_PDC = Objects.requireNonNull(NamespacedKey.fromString("buildmc:player_status"));

    private static boolean hasStatus(@NotNull Player player) {
        return player.getPersistentDataContainer().has(PLAYER_STATUS_PDC);
    }

    public static boolean doesStatusExist(String status) {
        if (status == null) return false;
        return StatusConfig.loadedStatuses.containsKey(status);
    }

    public static void setPlayerStatus(@NotNull Player player, String status, boolean join) {
        if (!doesStatusExist(status)) {
            if (join) {
                audiences.player(player).sendMessage(Component.translatable("messages.status.join-doesn't-exist"));
                player.getPersistentDataContainer().remove(PLAYER_STATUS_PDC);
            } else {
                audiences.player(player).sendMessage(Component.translatable("messages.status.not-found"));
                Sounds.playSound(player, Sounds.MISTAKE);
            }
            return;
        }

        StatusInstance s = StatusConfig.loadedStatuses.get(status);
        var allowed = s.allowPlayer(player);
        if (!allowed.equals(StatusInstance.AllowStatus.ALLOW)) {
            switch (allowed) {
                case NOT_IN_TEAM ->
                    audiences.player(player).sendMessage(Component.translatable("messages.status.not-in-team"));
                case MISSING_PERMISSION ->
                    audiences.player(player).sendMessage(Component.translatable("messages.status.no-permission"));
            }
            if (join) {
                player.getPersistentDataContainer().remove(PLAYER_STATUS_PDC);
                return;
            }
            Sounds.playSound(player, Sounds.MISTAKE);
            return;
        }

        Component c = s.getDisplay().asComponent().append(Component.text(player.getName()));

        String legacy = LegacyComponentSerializer.legacySection().serialize(c);

        player.setPlayerListName(legacy);
        player.setDisplayName(legacy);
        player.setCustomName(legacy);
        player.setCustomNameVisible(true);

        player.getPersistentDataContainer().set(
                PLAYER_STATUS_PDC,
                PersistentDataType.STRING,
                status
        );

        if (join) return;
        audiences.player(player).sendMessage(Component.translatable("messages.status.successfully-set"));
        Sounds.playSound(player, Sounds.SUCCESS);
    }

    public static void removePlayerStatus(@NotNull Player player) {
        player.getPersistentDataContainer().remove(PLAYER_STATUS_PDC);

        player.setPlayerListName(null);
        player.setDisplayName(null);
        player.setCustomName(null);
        player.setCustomNameVisible(false);
    }

    @EventHandler
    public static void onPlayerJoin(PlayerJoinEvent event) {
        if (hasStatus(event.getPlayer())) {
            Player player = event.getPlayer();
            setPlayerStatus(
                    player,
                    player.getPersistentDataContainer().get(PLAYER_STATUS_PDC, PersistentDataType.STRING),
                    true
            );
        }
    }

}
