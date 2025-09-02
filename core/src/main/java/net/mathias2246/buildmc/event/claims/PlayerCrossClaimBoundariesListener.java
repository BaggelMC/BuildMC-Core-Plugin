package net.mathias2246.buildmc.event.claims;

import net.mathias2246.buildmc.api.event.claims.PlayerEnterClaimEvent;
import net.mathias2246.buildmc.api.event.claims.PlayerLeaveClaimEvent;
import net.mathias2246.buildmc.claims.ClaimManager;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Objects;

import static net.mathias2246.buildmc.CoreMain.plugin;

/**
 * Listens for players crossing claim boundaries.
 * <p>
 * Fires {@link PlayerEnterClaimEvent} when a player enters a new claim,
 * and {@link PlayerLeaveClaimEvent} when a player leaves a claim.
 */
public class PlayerCrossClaimBoundariesListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;

        Chunk oldChunk = event.getFrom().getChunk();
        Chunk newChunk = event.getTo().getChunk();

        // Ignore movement within the same chunk
        if (oldChunk.equals(newChunk)) return;

        Player player = event.getPlayer();

        Long oldClaim = ClaimManager.getClaimId(oldChunk);
        Long newClaim = ClaimManager.getClaimId(newChunk);

        // If claim status hasn't changed (both null or both same claim), ignore
        if (Objects.equals(oldClaim, newClaim)) return;

        // Fire leave event if applicable
        plugin.getServer().getPluginManager().callEvent(new PlayerLeaveClaimEvent(
                player,
                oldClaim,
                newClaim
        ));

        // Fire enter event if applicable
        plugin.getServer().getPluginManager().callEvent(new PlayerEnterClaimEvent(
                player,
                oldClaim,
                newClaim
        ));
    }
}
