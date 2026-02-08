package net.mathias2246.buildmc.player;

import net.mathias2246.buildmc.api.event.player.PlayerFirstTimeJoinEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class FirstTimeJoinListener implements Listener {
    // Calls PlayerFirstTimeJoinEvent from PlayerJoinEvent
    @EventHandler
    public void onFirstTimeJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.getLastPlayed() != 0) return;
        PlayerFirstTimeJoinEvent e = new PlayerFirstTimeJoinEvent(player, event);
        Bukkit.getPluginManager().callEvent(e);
    }

}
