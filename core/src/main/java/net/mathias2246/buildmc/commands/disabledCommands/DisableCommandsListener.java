package net.mathias2246.buildmc.commands.disabledCommands;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class DisableCommandsListener implements Listener {

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage().toLowerCase();

        if (CoreMain.config.getBoolean("disable-reload-command") &&
                (msg.equals("/reload") || msg.equals("/rl") || msg.equals("/bukkit:reload"))) {
            event.setCancelled(true);
            CoreMain.pluginMain.sendMessage(event.getPlayer(), Component.translatable("messages.error.command-disabled"));
        }
        else if (CoreMain.config.getBoolean("disable-seed-command") &&
                (msg.equals("/seed") || msg.equals("/minecraft:seed"))) {
            event.setCancelled(true);
            CoreMain.pluginMain.sendMessage(event.getPlayer(), Component.translatable("messages.error.command-disabled"));
        }
    }


}
