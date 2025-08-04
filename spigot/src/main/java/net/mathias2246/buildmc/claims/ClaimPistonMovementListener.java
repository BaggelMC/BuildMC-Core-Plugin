package net.mathias2246.buildmc.claims;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import java.util.List;

public class ClaimPistonMovementListener implements Listener {

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        Block piston = event.getBlock();
        String pistonOwner = ClaimManager.getOwnerString(piston.getChunk());
        BlockFace direction = event.getDirection();
        List<Block> movedBlocks = event.getBlocks();

        for (Block movedBlock : movedBlocks) {
            Block destinationBlock = movedBlock.getRelative(direction);

            String fromOwner = ClaimManager.getOwnerString(movedBlock.getChunk());
            String toOwner = ClaimManager.getOwnerString(destinationBlock.getChunk());

            // Prevent if moving across different ownership
            if (!equalsOrNull(fromOwner, toOwner)) {
                event.setCancelled(true);
                return;
            }

            // Prevent if piston doesn't own the destination area
            if (!equalsOrNull(pistonOwner, toOwner)) {
                event.setCancelled(true);
                return;
            }

            // Prevent if piston doesn't own the block being moved
            if (!equalsOrNull(pistonOwner, fromOwner)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (!event.isSticky()) return;

        Block piston = event.getBlock();
        String pistonOwner = ClaimManager.getOwnerString(piston.getChunk());

        BlockFace direction = event.getDirection();
        Block blockToPull = piston.getRelative(direction.getOppositeFace()).getRelative(direction.getOppositeFace());
        Block pullDestination = blockToPull.getRelative(direction);

        String pulledBlockOwner = ClaimManager.getOwnerString(blockToPull.getChunk());
        String destinationOwner = ClaimManager.getOwnerString(pullDestination.getChunk());

        // Prevent pulling if piston doesn't own the block it's pulling
        if (!equalsOrNull(pistonOwner, pulledBlockOwner)) {
            event.setCancelled(true);
            return;
        }

        // Prevent pulling if piston doesn't own the destination
        if (!equalsOrNull(pistonOwner, destinationOwner)) {
            event.setCancelled(true);
            return;
        }

        // Prevent pulling if ownership changes between pulled block and destination
        if (!equalsOrNull(pulledBlockOwner, destinationOwner)) {
            event.setCancelled(true);
        }
    }



    private boolean equalsOrNull(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
}
