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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static net.mathias2246.buildmc.util.SoundUtil.*;

public class TeleportTimer extends PlayerTimer {
        public static final int seconds = CoreMain.plugin.getConfig().getInt("spawn-teleport.wait-for");

        public static int teleportCommandLogic(@NotNull Player player) {
            PlayerSpawnTeleportEvent e = new PlayerSpawnTeleportEvent(player, Bukkit.getWorlds().getFirst().getSpawnLocation());
            Bukkit.getPluginManager().callEvent(e);
            if (e.isCancelled()) {
                 AudienceUtil.sendMessage(player, Component.translatable("messages.spawn-teleport.not-working"));
                return 0;
            }

            var timer = new TeleportTimer(player, e.getTo());

            timer.start(0);

            return 1;
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
        }

        @Override
        protected void init() {
            registerInputListener();
        }

        @Override
        protected boolean shouldCancel() {
            return !player.isOnline() || playerInputReceived;
        }

        @Override
        protected void onCancel() {
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
