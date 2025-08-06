package net.mathias2246.buildmc.claims;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;

import static net.mathias2246.buildmc.CoreMain.audiences;
import static net.mathias2246.buildmc.CoreMain.claimManager;

public class ClaimPotionSplashEvent implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPotionSplash(PotionSplashEvent event) {
        ThrownPotion thrownPotion = event.getPotion();
        if (thrownPotion.getShooter() instanceof Player player) {
            if (!ClaimManager.isPlayerAllowed(claimManager, player, thrownPotion.getLocation())) {
                audiences.player(player).sendActionBar(Component.translatable("messages.claims.not-accessible.potion-splash"));
                event.setCancelled(true);
            }
        }
    }
}
