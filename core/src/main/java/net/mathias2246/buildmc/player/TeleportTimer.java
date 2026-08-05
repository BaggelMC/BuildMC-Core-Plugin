package net.mathias2246.buildmc.player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.event.player.PlayerSpawnTeleportEvent;
import net.mathias2246.buildmc.util.AudienceUtil;
import net.mathias2246.buildmc.util.Message;
import net.mathias2246.buildmc.util.PlayerTimer;
import net.mathias2246.buildmc.util.persistentDataTypes.PersistentDateTimeType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

import static net.mathias2246.buildmc.util.SoundUtil.*;

public class TeleportTimer extends PlayerTimer {

        public static final @NotNull NamespacedKey TELEPORT_COOLDOWN_KEY = new NamespacedKey("buildmc", "spawn_teleport_cooldown");
        public static final @NotNull NamespacedKey TELEPORT_ACTIVE_KEY = new NamespacedKey("buildmc", "spawn_teleport_active");


        public static @Nullable Integer cooldown = null;

        public static final int seconds = CoreMain.plugin.getConfig().getInt("spawn-teleport.wait-for");

        public static int teleportCommandLogic(@NotNull Player player) {
            PlayerSpawnTeleportEvent e = new PlayerSpawnTeleportEvent(player, Bukkit.getWorlds().getFirst().getSpawnLocation());
            Bukkit.getPluginManager().callEvent(e);
            if (e.isCancelled()) {
                AudienceUtil.sendMessage(player, Component.translatable("messages.spawn-teleport.not-working"));
                CoreMain.soundManager.playSound(player, mistake);
                return 0;
            }

            var pdc = player.getPersistentDataContainer();

            if (pdc.has(TELEPORT_ACTIVE_KEY)) {
                return 0;
            }

            if (cooldown != null && pdc.has(TELEPORT_COOLDOWN_KEY)) {
                var time = pdc.getOrDefault(TELEPORT_COOLDOWN_KEY, PersistentDateTimeType.INSTANCE, LocalDateTime.MIN);

                if (time.equals(LocalDateTime.MIN)) {
                    pdc.remove(TELEPORT_COOLDOWN_KEY);
                }

                var timeAfterCooldown = time.plusSeconds(cooldown);
                var now = LocalDateTime.now();

                if (timeAfterCooldown.isAfter(now)) {
                    var remainingSeconds = Duration.between(now, timeAfterCooldown).getSeconds();

                    AudienceUtil.sendMessage(player, Message.msg(player, "messages.teleport.cooldown", Map.of("cooldown", formatCooldown(remainingSeconds))));
                    CoreMain.soundManager.playSound(player, mistake);
                    return 0;
                }
            }


            var timer = new TeleportTimer(player, e.getTo());

            timer.start(0);

            return 1;
        }

        private static String formatCooldown(long totalSeconds) {
            if (totalSeconds <= 0) {
                return "0s";
            }

            if (totalSeconds > 60) {
                long minutes = (long) Math.ceil(totalSeconds / 60.0);
                return minutes + "m";
            }

            return totalSeconds + "s";
        }

        private boolean playerInputReceived = false;
        private PacketAdapter inputListener;

        private final @NotNull Location to;

        public TeleportTimer(@NotNull Player player, @NotNull Location to) {
            super(CoreMain.plugin, player, seconds, 20);
            this.to = to;
        }

        @Override
        public void onExit() {
            unregisterInputListener();
            AudienceUtil.sendMessage(player, Component.translatable("messages.teleport.successful"));
            player.teleport(to, PlayerTeleportEvent.TeleportCause.PLUGIN);
            CoreMain.soundManager.playSound(player, success);
            player.getPersistentDataContainer().remove(TELEPORT_ACTIVE_KEY);
            player.getPersistentDataContainer().set(TELEPORT_COOLDOWN_KEY, PersistentDateTimeType.INSTANCE, LocalDateTime.now());
        }

        @Override
        protected void init() {
            player.getPersistentDataContainer().set(TELEPORT_ACTIVE_KEY, PersistentDataType.BOOLEAN, true);
            registerInputListener();
        }

        @Override
        protected boolean shouldCancel() {
            return !player.isOnline() || playerInputReceived;
        }

        @Override
        protected void onCancel() {
            player.getPersistentDataContainer().remove(TELEPORT_ACTIVE_KEY);
            unregisterInputListener();
            AudienceUtil.sendActionBar(player, Component.translatable("messages.teleport.cancelled"));
            CoreMain.soundManager.playSound(player, mistake);
        }

        @Override
        protected void onStep() {

            TextReplacementConfig secondsReplacement =
                    TextReplacementConfig.builder()
                            .matchLiteral("%seconds%")
                            .replacement(String.valueOf(steps - currentStep))
                            .build();

            AudienceUtil.sendActionBar(player,
                    Message.msg(player,"messages.teleport.counter").replaceText(secondsReplacement)
            );
            CoreMain.soundManager.playSound(player, notification);
        }

    private void registerInputListener() {
        inputListener = new PacketAdapter(CoreMain.plugin, PacketType.Play.Client.STEER_VEHICLE) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (!event.getPlayer().getUniqueId().equals(player.getUniqueId())) return;

                try {
                    Object handle = event.getPacket().getHandle();
                    Object input = handle.getClass().getMethod("input").invoke(handle);
                    Class<?> inputClass = input.getClass();

                    boolean forward  = (boolean) inputClass.getMethod("forward").invoke(input);
                    boolean backward = (boolean) inputClass.getMethod("backward").invoke(input);
                    boolean left     = (boolean) inputClass.getMethod("left").invoke(input);
                    boolean right    = (boolean) inputClass.getMethod("right").invoke(input);
                    boolean jump     = (boolean) inputClass.getMethod("jump").invoke(input);

                    if (forward || backward || left || right || jump) {
                        playerInputReceived = true;
                    }
                } catch (Exception e) {
                    CoreMain.plugin.getLogger().warning("Failed to read STEER_VEHICLE packet: " + e.getMessage());
                }
            }
        };

        ProtocolLibrary.getProtocolManager().addPacketListener(inputListener);
    }

    private void unregisterInputListener() {
        if (inputListener != null) {
            ProtocolLibrary.getProtocolManager().removePacketListener(inputListener);
            inputListener = null;
        }
    }
}
