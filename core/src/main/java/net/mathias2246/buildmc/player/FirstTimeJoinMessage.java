package net.mathias2246.buildmc.player;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.api.event.player.PlayerFirstTimeJoinEvent;
import net.mathias2246.buildmc.util.Message;
import net.mathias2246.buildmc.util.SoundUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static net.mathias2246.buildmc.CoreMain.plugin;

public class FirstTimeJoinMessage implements Listener {

    public FirstTimeJoinMessage() {
    }

    @EventHandler
    public void onPlayerFistTimeJoin(PlayerFirstTimeJoinEvent event) {
        showMessage(event.getPlayer());
    }

    public void showMessage(@NotNull Player player) {
        Component msg = Message.msg(player, "messages.first-time-join.message", Map.of("player_name", player.getName()));
        plugin.sendMessage(player, msg);
        plugin.getSoundManager().playSound(player, SoundUtil.notification);
    }

}
