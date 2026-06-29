package net.mathias2246.buildmc.ui.claims;

import net.mathias2246.buildmc.api.event.claims.ClaimCreateEvent;
import net.mathias2246.buildmc.api.event.claims.ClaimRemoveEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;


public class ClaimSelectMenuListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClaimCreate(ClaimCreateEvent event) {
        ClaimSelectMenu.refreshAll();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClaimRemove(ClaimRemoveEvent event) {
        ClaimSelectMenu.refreshAll();
    }
}