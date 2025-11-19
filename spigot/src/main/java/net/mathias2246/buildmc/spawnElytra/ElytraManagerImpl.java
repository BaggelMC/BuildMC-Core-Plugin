package net.mathias2246.buildmc.spawnElytra;

import net.mathias2246.buildmc.api.spawnEyltra.ElytraManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.mathias2246.buildmc.Main.plugin;

public record ElytraManagerImpl(ElytraZoneManager zoneManager) implements ElytraManager {

    public ElytraManagerImpl(@NotNull ElytraZoneManager zoneManager) {
        this.zoneManager = zoneManager;
    }

    @Override
    public boolean isElytraEnabled() {
        return plugin.getConfig().getBoolean("spawn-elytra.enabled", true);
    }

    @Override
    public boolean isInElytraZone(@NotNull Player player) {
        return zoneManager.isInZone(player);
    }

    @Override
    public boolean isInElytraZone(@NotNull Location location) {
        return zoneManager.isInZone(location);
    }

    @Override
    public void registerZone(@NotNull Location pos1, @NotNull Location pos2, @NotNull World world) {
        zoneManager.registerZone(pos1, pos2, world);
    }

    @Override
    public void setPlayerFlying(@NotNull Player player, boolean flying) {
        if (flying) {
            SpawnBoostListener.setPlayerFlying(player);
        } else {
            SpawnBoostListener.stopFlying(player);
        }
    }

    @Override
    public boolean isUsingSpawnElytra(@NotNull Player player) {
        return SpawnBoostListener.isUsingSpawnElytra(player);
    }

    @Override
    public boolean isUsingSpawnElytra(@NotNull Entity entity) {
        return SpawnBoostListener.isUsingSpawnElytra(entity);
    }

    @Override
    public boolean hasBoosted(@NotNull Player player) {
        return SpawnBoostListener.isPlayerBoosted(player);
    }

    @Override
    public boolean hasBoosted(@NotNull Entity entity) {
        return SpawnBoostListener.isPlayerBoosted(entity);
    }

    @Override
    public void resetBoost(@NotNull Player player) {
        SpawnBoostListener.resetBoost(player);
    }

    @Override
    public void resetBoost(@NotNull Entity entity) {
        SpawnBoostListener.resetBoost(entity);
    }

    @Override
    public void applyBoost(@NotNull Player player, int multiplier, double verticalVelocity) {
        SpawnBoostListener.applyBoost(player, multiplier, verticalVelocity);
    }

    @Override
    public void applyBoost(@NotNull Player player) {

        int multiplyValue = plugin.getConfig().getInt("spawn-elytra.strength", 2);
        double verticalVelocity = 1.2;

        SpawnBoostListener.applyBoost(player, multiplyValue, verticalVelocity);
    }

    @Override
    public void applyRawBoost(@NotNull Player player, int multiplier, double verticalVelocity) {
        SpawnBoostListener.applyRawBoost(player, multiplier, verticalVelocity);
    }

    @Override
    public void applyRawBoost(@NotNull Player player) {
        int multiplyValue = plugin.getConfig().getInt("spawn-elytra.strength", 2);
        double verticalVelocity = 1.2;

        SpawnBoostListener.applyRawBoost(player, multiplyValue, verticalVelocity);
    }
}
