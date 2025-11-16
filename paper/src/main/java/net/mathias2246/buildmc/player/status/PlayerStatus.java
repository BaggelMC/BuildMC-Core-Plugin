package net.mathias2246.buildmc.player.status;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.status.StatusInstance;
import net.mathias2246.buildmc.platform.SoundManagerPaperImpl;
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
        return CoreMain.statusesRegistry.contains(Objects.requireNonNull(NamespacedKey.fromString("buildmc:" + status)));
    }

    public static void setPlayerStatus(@NotNull Player player, String status, boolean join) {
        if (!doesStatusExist(status)) {
            if (join) {
                player.sendMessage(Component.translatable("messages.status.join-doesn't-exist"));
                player.getPersistentDataContainer().remove(PLAYER_STATUS_PDC);
            } else {
                player.sendMessage(Component.translatable("messages.status.not-found"));
                CoreMain.soundManager.playSound(player, SoundManagerPaperImpl.mistake);
            }
            return;
        }

        StatusInstance s = CoreMain.statusesRegistry.getOrThrow(Objects.requireNonNull(Objects.requireNonNull(NamespacedKey.fromString("buildmc:" + status)).key()));
        var allowed = s.allowPlayer(player);
        if (!allowed.equals(StatusInstance.AllowStatus.ALLOW)) {
            switch (allowed) {
                case NOT_IN_TEAM -> player.sendMessage(Component.translatable("messages.status.not-in-team"));
                case MISSING_PERMISSION -> player.sendMessage(Component.translatable("messages.status.no-permission"));
            }
            if (join) {
                player.getPersistentDataContainer().remove(PLAYER_STATUS_PDC);
                return;
            }
            CoreMain.soundManager.playSound(player, SoundManagerPaperImpl.mistake);
            return;
        }

        Component c = s.getDisplay().asComponent().append(player.name());

        player.playerListName(c);
        player.displayName(c);
        player.customName(c);
        player.setCustomNameVisible(true);

        player.getPersistentDataContainer().set(
                PLAYER_STATUS_PDC,
                PersistentDataType.STRING,
                status
        );

        if (join) return;
        player.sendMessage(Component.translatable("messages.status.successfully-set"));
        CoreMain.soundManager.playSound(player, SoundManagerPaperImpl.success);
    }

    public static void removePlayerStatus(@NotNull Player player) {
        player.getPersistentDataContainer().remove(PLAYER_STATUS_PDC);

        player.playerListName(null);
        player.displayName(null);
        player.customName(null);
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