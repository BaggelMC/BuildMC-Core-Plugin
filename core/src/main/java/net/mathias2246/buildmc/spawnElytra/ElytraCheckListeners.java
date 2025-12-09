package net.mathias2246.buildmc.spawnElytra;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import static net.mathias2246.buildmc.spawnElytra.SpawnElytraUtil.*;

public record ElytraCheckListeners(ElytraZoneManager zoneManager, boolean boostEnabled, double multiplyValue) implements Listener {
    @EventHandler
    public void onDoubleJump(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (!isSurvival(player)) return;

        if (!zoneManager.isInZone(player)) return;

        event.setCancelled(true);

        if (player.isGliding()) return;

        setPlayerFlying(player);

        if (boostEnabled && !isPlayerBoosted(player)) {
            CoreMain.plugin.sendPlayerActionBar(player, Component.translatable("messages.spawn-elytra.boost-hint"));
        }
    }

    @EventHandler
    public void onSwapItem(PlayerSwapHandItemsEvent event) {
        if (!boostEnabled) return;

        Player player = event.getPlayer();
        if (!isUsingSpawnElytra(player) || isPlayerBoosted(player)) return;

        event.setCancelled(true);

        applyBoost(player, multiplyValue, 1.2);

    }

    @EventHandler
    public static void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (isUsingSpawnElytra(player) && (
                event.getCause() == EntityDamageEvent.DamageCause.FALL ||
                        event.getCause() == EntityDamageEvent.DamageCause.FLY_INTO_WALL)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public static void onToggleGlide(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (isUsingSpawnElytra(player)) {
            event.setCancelled(true);
        }
    }

    // Fixes Issue where players are gliding when riding entities that are not on the ground
    @EventHandler
    public void onEntityMount(EntityMountEvent event) {
        Entity entity = event.getEntity();
        if (isUsingSpawnElytra(entity)) {
            if (entity instanceof Player player) stopFlying(player);
        }
    }

    // Fixes elytra not closing when changing gamemode
    @EventHandler
    public void onGamemodeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        GameMode oldMode = player.getGameMode();
        GameMode newMode = event.getNewGameMode();

        // Only stop flying if leaving survival/adventure -> something else,
        // and the player was using the spawn elytra
        if (isSurvival(oldMode) && !isSurvival(newMode) && isUsingSpawnElytra(player)) {
            stopFlying(player);
        }
    }
}
