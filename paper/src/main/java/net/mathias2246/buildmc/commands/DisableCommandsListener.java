package net.mathias2246.buildmc.commands;

import net.mathias2246.buildmc.util.Message;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import static net.mathias2246.buildmc.Main.config;

public class DisableCommandsListener implements Listener {

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage().toLowerCase();
        if (config.getBoolean("disable-reload-command") &&
                (msg.equals("/reload") || msg.equals("/rl") || msg.equals("/bukkit:reload"))) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Message.msg(event.getPlayer(), "messages.error.command-disabled"));
        }

        if (config.getBoolean("disable-seed-command") &&
                (msg.equals("/seed") || msg.equals("/minecraft:seed"))) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Message.msg(event.getPlayer(), "messages.error.command-disabled"));
        }
    }


}
