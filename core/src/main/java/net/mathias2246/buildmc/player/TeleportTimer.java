package net.mathias2246.buildmc.player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.event.player.PlayerSpawnTeleportPreConditionEvent;
import net.mathias2246.buildmc.util.Message;
import net.mathias2246.buildmc.util.PlayerTimer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import static net.mathias2246.buildmc.util.SoundUtil.*;

public class TeleportTimer extends PlayerTimer {
        public static final int seconds = CoreMain.plugin.getConfig().getInt("spawn-teleport.wait-for");

        public static int teleportCommandLogic(@NotNull Player player) {
            PlayerSpawnTeleportPreConditionEvent e = new PlayerSpawnTeleportPreConditionEvent(player, Bukkit.getWorlds().getFirst().getSpawnLocation());
            Bukkit.getPluginManager().callEvent(e);
            if (e.isCancelled()) {
                CoreMain.plugin.sendMessage(player, Component.translatable("messages.spawn-teleport.not-working"));
                return 0;
            }

            var timer = new TeleportTimer(player, e.getTo());

            timer.start(0);

            return 1;
        }

        private Vector previousPosition;

        private final @NotNull Location to;

        public TeleportTimer(@NotNull Player player, @NotNull Location to) {
            super(CoreMain.plugin, player, seconds, 20);
            this.to = to;
        }

        @Override
        public void onExit() {
            CoreMain.plugin.sendMessage(player, Component.translatable("messages.teleport.successful"));
            player.teleport(to, PlayerTeleportEvent.TeleportCause.PLUGIN);
            CoreMain.soundManager.playSound(player, success);
        }

        @Override
        protected void init() {
            previousPosition = player.getLocation().toVector();
        }

        @Override
        protected boolean shouldCancel() {
            // TODO: If possible, cancel when the player inputs a movement
            //  so that he can teleport when being pushed around by other players or redstone contraptions, etc.
            return !player.isOnline() || !player.getLocation().toVector().equals(previousPosition);
        }

        @Override
        protected void onCancel() {
            CoreMain.plugin.sendMessage(player, Component.translatable("messages.teleport.cancelled"));
            CoreMain.soundManager.playSound(player, mistake);
        }

        @Override
        protected void onStep() {

            TextReplacementConfig r = TextReplacementConfig.builder().matchLiteral("%seconds%").replacement(String.valueOf(steps-currentStep)).build();

            CoreMain.plugin.sendPlayerActionBar(
                    player,
                    Message.msg(player,"messages.teleport.counter").replaceText(r)
            );
            CoreMain.soundManager.playSound(player, notification);
        }
}
