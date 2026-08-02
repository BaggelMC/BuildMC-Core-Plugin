package net.mathias2246.buildmc.event.claims;

import net.mathias2246.buildmc.api.event.claims.PlayerChangeClaimEvent;
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
 * Fires {@link PlayerChangeClaimEvent} when a player moves to a new claim.
 */
public class PlayerCrossClaimBoundariesListener implements Listener {
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location oldLoc = event.getFrom();
        Location newLoc = event.getTo();

        // Ignore movement within the same chunk
        if (oldLoc.getChunk().equals(newLoc.getChunk())) return;

        Player player = event.getPlayer();

        Long oldClaim = ClaimManager.getClaimId(oldLoc);
        Long newClaim = ClaimManager.getClaimId(newLoc);

        // If claim status hasn’t changed, ignore
        if (Objects.equals(oldClaim, newClaim)) return;

        plugin.getServer().getPluginManager().callEvent(new PlayerChangeClaimEvent(
                player,
                oldClaim,
                newClaim
        ));
    }
}
