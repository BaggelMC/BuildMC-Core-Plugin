package net.mathias2246.buildmc.claims;

import net.kyori.adventure.text.Component;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import static net.mathias2246.buildmc.Main.audiences;
import static net.mathias2246.buildmc.Main.claimManager;

public class ClaimSignEditListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignChange(SignChangeEvent event) {

        Player player = event.getPlayer();

        if (!ClaimManager.isPlayerAllowed(claimManager, player, event.getBlock().getLocation())) {
            // Cancel the sign change
            event.setCancelled(true);

            if (event.getBlock().getState() instanceof Sign sign) {
                sign.update(); // Reset visual state
            }

            // Send feedback to the player
            audiences.player(player).sendActionBar(Component.translatable("messages.claims.not-accessible.sign"));
        }
    }
}