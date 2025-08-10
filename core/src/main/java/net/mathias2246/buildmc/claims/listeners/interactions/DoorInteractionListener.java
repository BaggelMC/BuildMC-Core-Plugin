package net.mathias2246.buildmc.claims.listeners.interactions;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.tags.MaterialTag;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Objects;
import java.util.Set;

public class DoorInteractionListener implements Listener {

    private static final MaterialTag doors = new MaterialTag(
            Set.of(
                    Tag.DOORS,
                    Tag.TRAPDOORS,
                    Tag.FENCE_GATES
            ),
            Objects.requireNonNull(NamespacedKey.fromString("buildmc:interaction/door"))
    );

    @EventHandler
    public void onDoorInteraction(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        Block block = event.getClickedBlock();
        if (!block.getType().isInteractable()) return;

        if (!doors.isTagged(block.getType())) return;

        Player player = event.getPlayer();
        if (!ClaimManager.isPlayerAllowed(CoreMain.claimManager, player, block.getLocation())) {
            CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.interact"));
            event.setCancelled(true);
        }
    }

}
