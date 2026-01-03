package net.mathias2246.buildmc.status;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.event.player.StatusChangeEvent;
import net.mathias2246.buildmc.api.status.StatusInstance;
import net.mathias2246.buildmc.util.SoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static net.mathias2246.buildmc.CoreMain.bukkitAudiences;

public class PlayerStatusUtil implements Listener {

    public static final @NotNull NamespacedKey PLAYER_STATUS_PDC = Objects.requireNonNull(NamespacedKey.fromString("buildmc:player_status"));

    private static boolean hasStatus(@NotNull Player player) {
        return player.getPersistentDataContainer().has(PLAYER_STATUS_PDC);
    }

    public static void reloadPlayerStatus(@NotNull Player player) {

        String status = player.getPersistentDataContainer().get(PLAYER_STATUS_PDC, PersistentDataType.STRING);
        if (status == null) return;
        StatusInstance s;
        try {
            s = CoreMain.statusesRegistry.getOrThrow(Objects.requireNonNull(NamespacedKey.fromString("buildmc:" + status)));
        } catch (Exception ignore) {
            return;
        }

        var allowed = s.allowPlayer(player);
        if (!allowed.equals(StatusInstance.AllowStatus.ALLOW)) {
            switch (allowed) {
                case NOT_IN_TEAM, MISSING_PERMISSION ->
                {
                    return;
                }
            }
        }

        Component c = s.getDisplay().asComponent().append(Component.text(player.getName()));

        CoreMain.statusManager.setPlayerName(player, c);

        player.getPersistentDataContainer().set(
                PLAYER_STATUS_PDC,
                PersistentDataType.STRING,
                status
        );
    }

    public static void setPlayerStatus(@NotNull Player player, String status, boolean join) {
        status = status.toLowerCase();
        if (!doesStatusExist(status)) {
            if (join) {
                CoreMain.plugin.sendMessage(player, Component.translatable("messages.status.join-doesn't-exist"));
                player.getPersistentDataContainer().remove(PLAYER_STATUS_PDC);
            } else {
                CoreMain.plugin.sendMessage(player, Component.translatable("messages.status.not-found"));
                CoreMain.soundManager.playSound(player, SoundUtil.mistake);
            }
            return;
        }
        @Nullable StatusInstance old = CoreMain.statusesRegistry.get(Objects.requireNonNull(NamespacedKey.fromString("buildmc:" + player.getPersistentDataContainer().get(PLAYER_STATUS_PDC, PersistentDataType.STRING))));

        StatusInstance s = CoreMain.statusesRegistry.getOrThrow(Objects.requireNonNull(NamespacedKey.fromString("buildmc:" + status)));

        if (!join) {
            StatusChangeEvent e = new StatusChangeEvent(player, old, s);
            Bukkit.getPluginManager().callEvent(e);
            if (e.isCancelled()) {
                CoreMain.plugin.sendMessage(player, Component.translatable("messages.status.cannot-set"));
                return;
            } else if (e.getNewStatus() == null) {
                CoreMain.statusManager.forceRemovePlayerStatus(player);
                return;
            }
        }

        var allowed = s.allowPlayer(player);
        if (!allowed.equals(StatusInstance.AllowStatus.ALLOW)) {
            switch (allowed) {
                case NOT_IN_TEAM ->
                        CoreMain.plugin.sendMessage(player, Component.translatable("messages.status.not-in-team"));
                case MISSING_PERMISSION ->
                        CoreMain.plugin.sendMessage(player, Component.translatable("messages.status.no-permission"));
            }
            if (join) {
                player.getPersistentDataContainer().remove(PLAYER_STATUS_PDC);
                return;
            }
            CoreMain.soundManager.playSound(player, SoundUtil.mistake);
            return;
        }

        Component c = s.getDisplay().asComponent().append(Component.text(player.getName()));

        CoreMain.statusManager.setPlayerName(player, c);

        player.getPersistentDataContainer().set(
                PLAYER_STATUS_PDC,
                PersistentDataType.STRING,
                status
        );

        if (join) return;
        bukkitAudiences.player(player).sendMessage(Component.translatable("messages.status.successfully-set"));
        CoreMain.soundManager.playSound(player, SoundUtil.success);
    }

    public static boolean doesStatusExist(String status) {
        if (status == null) return false;
        return CoreMain.statusesRegistry.contains(Objects.requireNonNull(NamespacedKey.fromString("buildmc:" + status)));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (hasStatus(event.getPlayer())) {
            Player player = event.getPlayer();
            CoreMain.statusManager.setPlayerStatus(
                    player,
                    player.getPersistentDataContainer().get(PLAYER_STATUS_PDC, PersistentDataType.STRING),
                    true
            );
        }
    }

}
