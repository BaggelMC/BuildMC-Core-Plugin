package net.mathias2246.buildmc.event.claims;

import net.mathias2246.buildmc.api.event.claims.PlayerEnterClaimEvent;
import net.mathias2246.buildmc.api.event.claims.PlayerLeaveClaimEvent;
import net.mathias2246.buildmc.claims.ClaimManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Objects;

import static net.mathias2246.buildmc.CoreMain.plugin;

/**
 * Listens for players crossing claim boundaries.
 * <p>
 * Fires {@link PlayerLeaveClaimEvent} when a player leaves a claim,
 * and {@link PlayerEnterClaimEvent} when a player enters a new claim.
 */
public class PlayerCrossClaimBoundariesListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;

        Location oldLoc = event.getFrom();
        Location newLoc = event.getTo();

        // Ignore movement within the same chunk
        if (oldLoc.equals(newLoc)) return;

        Player player = event.getPlayer();

        Long oldClaim = ClaimManager.getClaimId(oldLoc);
        Long newClaim = ClaimManager.getClaimId(newLoc);

        // If claim status hasn’t changed, ignore
        if (Objects.equals(oldClaim, newClaim)) return;

        // Fire leave event only if player was inside a claim
        if (oldClaim != null) {
            plugin.getServer().getPluginManager().callEvent(new PlayerLeaveClaimEvent(
                    player,
                    oldClaim,
                    newClaim
            ));
        }

        // Fire enter event only if the player is entering a claim
        if (newClaim != null) {
            plugin.getServer().getPluginManager().callEvent(new PlayerEnterClaimEvent(
                    player,
                    oldClaim,
                    newClaim
            ));
        }
    }
}
