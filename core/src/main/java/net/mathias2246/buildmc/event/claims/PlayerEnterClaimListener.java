package net.mathias2246.buildmc.event.claims;

import net.mathias2246.buildmc.api.event.claims.PlayerEnterClaimEvent;
import net.mathias2246.buildmc.claims.ClaimManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Objects;

import static net.mathias2246.buildmc.CoreMain.plugin;

public class PlayerEnterClaimListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {

        if (event.getTo() == null) return;

        var oldCp = event.getFrom().getChunk();
        var newCp = event.getTo().getChunk();


        if (oldCp.equals(newCp)) return;

        var player = event.getPlayer();

        Long oldClaim = ClaimManager.getClaimId(oldCp);
        Long newClaim = ClaimManager.getClaimId(newCp);

        if (!ClaimManager.isClaimed(newCp) && Objects.equals(oldClaim, newClaim)) return;

        plugin.getServer().getPluginManager().callEvent(new PlayerEnterClaimEvent(
                player,
                oldClaim,
                newClaim
        ));
    }

}
