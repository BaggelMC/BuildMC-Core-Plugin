package net.mathias2246.buildmc.claims.listeners;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.claims.ProtectionFlag;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.projectiles.ProjectileSource;

public class ClaimProjectileInteractListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProjectileHit(ProjectileHitEvent event) {
        Block block = event.getHitBlock();
        if (block == null) return;

        Projectile projectile = event.getEntity();
        ProjectileSource projectileSource = projectile.getShooter();

        if (!(projectileSource instanceof Player player)) return;

        if (!ClaimManager.isPlayerAllowed(player, ProtectionFlag.PROJECTILE_INTERACTIONS, block.getLocation())) {
            projectile.remove();
            event.setCancelled(true);
            CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.interact"));
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectileInteract(EntityInteractEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof Projectile projectile)) return;

        ProjectileSource shooter = projectile.getShooter();
        if (!(shooter instanceof Player player)) return;

        if (!ClaimManager.isPlayerAllowed(player, ProtectionFlag.PROJECTILE_INTERACTIONS, event.getBlock().getLocation())) {
            event.setCancelled(true);

            // Sending should already be handled by onProjectileHit()
//            CoreMain.mainClass.sendPlayerActionBar(
//                    player,
//                    Component.translatable("messages.claims.not-accessible.interact")
//            );
        }
    }
}
