package net.mathias2246.buildmc.spawnElytra;

import net.mathias2246.buildmc.api.spawnEyltra.ElytraManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.mathias2246.buildmc.Main.plugin;

public class ElytraManagerImpl implements ElytraManager {

    private final ElytraZoneManager zoneManager;
    private final SpawnElytraConfig config;

    public ElytraManagerImpl(@NotNull ElytraZoneManager zoneManager, SpawnElytraConfig config) {
        this.zoneManager = zoneManager;
        this.config = config;
    }

    @Override
    public boolean isElytraEnabled() {
        return config.enabled;
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
            SpawnElytraUtil.setPlayerFlying(player);
        } else {
            SpawnElytraUtil.stopFlying(player);
        }
    }

    @Override
    public boolean isUsingSpawnElytra(@NotNull Player player) {
        return SpawnElytraUtil.isUsingSpawnElytra(player);
    }

    @Override
    public boolean isUsingSpawnElytra(@NotNull Entity entity) {
        return SpawnElytraUtil.isUsingSpawnElytra(entity);
    }

    @Override
    public boolean hasBoosted(@NotNull Player player) {
        return SpawnElytraUtil.isPlayerBoosted(player);
    }

    @Override
    public boolean hasBoosted(@NotNull Entity entity) {
        return SpawnElytraUtil.isPlayerBoosted(entity);
    }

    @Override
    public void resetBoost(@NotNull Player player) {
        SpawnElytraUtil.resetBoost(player);
    }

    @Override
    public void resetBoost(@NotNull Entity entity) {
        SpawnElytraUtil.resetBoost(entity);
    }

    @Override
    public void applyBoost(@NotNull Player player, int multiplier, double verticalVelocity) {
        SpawnElytraUtil.applyBoost(player, multiplier, verticalVelocity);
    }

    @Override
    public void applyBoost(@NotNull Player player) {

        int multiplyValue = config.strength;
        double verticalVelocity = 1.2;

        applyBoost(player, multiplyValue, verticalVelocity);
    }

    @Override
    public void applyRawBoost(@NotNull Player player, int multiplier, double verticalVelocity) {
        SpawnElytraUtil.applyRawBoost(player, multiplier, verticalVelocity);
    }

    @Override
    public void applyRawBoost(@NotNull Player player) {
        int multiplyValue = config.strength;
        double verticalVelocity = 1.2;

        SpawnElytraUtil.applyRawBoost(player, multiplyValue, verticalVelocity);
    }
}
