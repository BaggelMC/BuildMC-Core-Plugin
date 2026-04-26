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
             AudienceUtil.sendMessage(player, Component.translatable("messages.teleport.cancelled"));
            CoreMain.soundManager.playSound(player, mistake);
        }

        @Override
        protected void onStep() {

            TextReplacementConfig secondsReplacement =
                    TextReplacementConfig.builder()
                            .matchLiteral("%seconds%")
                            .replacement(String.valueOf(steps - currentStep))
                            .build();

            AudienceUtil.sendMessage(player, 
                    Message.msg(player,"messages.teleport.counter").replaceText(secondsReplacement)
            );
            CoreMain.soundManager.playSound(player, notification);
        }

    private void registerInputListener() {
        List<PacketType> packets = new ArrayList<>(List.of(
                PacketType.Play.Client.POSITION,
                PacketType.Play.Client.POSITION_LOOK
        ));

        inputListener = new PacketAdapter(CoreMain.plugin, packets) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                    playerInputReceived = true;
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
