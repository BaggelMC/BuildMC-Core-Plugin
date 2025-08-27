package net.mathias2246.buildmc.claims.listeners;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.claims.ProtectionFlag;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;

public class ClaimPotionSplashListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPotionSplash(PotionSplashEvent event) {
        ThrownPotion thrownPotion = event.getPotion();
        if (thrownPotion.getShooter() instanceof Player player) {
            if (!ClaimManager.isPlayerAllowed(player, ProtectionFlag.SPLASH_POTIONS, thrownPotion.getLocation())) {
                CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.potion-splash"));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPotionSplash(LingeringPotionSplashEvent event) {
        ThrownPotion thrownPotion = event.getEntity();
        if (thrownPotion.getShooter() instanceof Player player) {
            if (!ClaimManager.isPlayerAllowed(player, ProtectionFlag.SPLASH_POTIONS, thrownPotion.getLocation())) {
                CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.potion-splash"));
                event.setCancelled(true);
            }
        }
    }
}