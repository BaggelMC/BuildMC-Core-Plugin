package net.mathias2246.buildmc.claims.listeners;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.claims.ClaimManager;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class ClaimSignEditListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignChange(SignChangeEvent event) {

        Player player = event.getPlayer();

        if (!ClaimManager.isPlayerAllowed(CoreMain.claimManager, player, event.getBlock().getLocation())) {
            // Cancel the sign change
            event.setCancelled(true);

            if (event.getBlock().getState() instanceof Sign sign) {
                sign.update(); // Reset visual state
            }

            // Send feedback to the player
            CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.sign"));
        }
    }
}