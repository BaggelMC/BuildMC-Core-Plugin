package net.mathias2246.buildmc.claims;

import net.mathias2246.buildmc.commands.claim.ClaimCreate;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class ClaimSelectionChangeWorld implements Listener {

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        ClaimCreate.removeSelectionData(event.getPlayer());
    }

}
