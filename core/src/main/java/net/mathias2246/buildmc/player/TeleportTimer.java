package net.mathias2246.buildmc.player;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.util.Message;
import net.mathias2246.buildmc.util.PlayerTimer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class TeleportTimer extends PlayerTimer {

    static final Sound notification;
    static final Sound mistake;
    static final Sound success;


    static {
        //noinspection PatternValidation
        notification = Sound.sound(
                Key.key(CoreMain.plugin.getConfig().getString("sounds.notification", "minecraft:entity.item.pickup")),
                Sound.Source.MASTER,
                1f,
                1f
        );
        //noinspection PatternValidation
        mistake = Sound.sound(
                Key.key(CoreMain.plugin.getConfig().getString("sounds.mistake", "minecraft:block.note_block.snare")),
                Sound.Source.MASTER,
                1f,
                1f
        );
        //noinspection PatternValidation
        success = Sound.sound(
                Key.key(CoreMain.plugin.getConfig().getString("sounds.success", "minecraft:block.note_block.bell")),
                Sound.Source.MASTER,
                1f,
                1f
        );
    }

        public static final int seconds = CoreMain.plugin.getConfig().getInt("spawn-teleport.wait-for");

        private Vector previousPosition;

        private final @NotNull Location to;

        public TeleportTimer(@NotNull Player player, @NotNull Location to) {
            super(CoreMain.plugin, player, seconds, 20);
            this.to = to;
        }

        @Override
        public void onExit() {
            CoreMain.mainClass.sendMessage(player, Component.translatable("messages.teleport.successful"));
            player.teleport(to);
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
            CoreMain.mainClass.sendMessage(player, Component.translatable("messages.teleport.cancelled"));
            CoreMain.soundManager.playSound(player, mistake);
        }

        @Override
        protected void onStep() {

            TextReplacementConfig r = TextReplacementConfig.builder().matchLiteral("%seconds%").replacement(String.valueOf(steps-currentStep)).build();

            CoreMain.mainClass.sendPlayerActionBar(
                    player,
                    Message.msg(player,"messages.teleport.counter").replaceText(r)
            );
            CoreMain.soundManager.playSound(player, notification);
        }
}
