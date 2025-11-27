package net.mathias2246.buildmc.permissions;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PermissionListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PermissionGroupManager.recalculatePermissions(event.getPlayer());
    }

}
