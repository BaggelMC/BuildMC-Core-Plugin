package net.mathias2246.buildmc.spawnElytra;

import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.util.AudienceUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/** Class for storing and managing the spawn-elytra zone */
public class ElytraZoneManager {

    private final SpawnElytraConfig config;

    private Location pos1;
    private Location pos2;
    private BoundingBox boundingBox;
    private World world;

    public ElytraZoneManager(@NotNull SpawnElytraConfig config) {
        this.config = config;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        if (this.world == world) return;

        pos1.setWorld(world);
        pos2.setWorld(world);
        this.world = world;
        saveZoneToConfig();
    }

    public void setPos1(@NotNull CommandSender sender, @NotNull Location loc) {
        pos1 = loc;
        AudienceUtil.sendMessage(sender, Component.translatable("messages.spawn-elytra.pos1-set"));
        tryCreateZone(sender);
    }

    public void setPos2(@NotNull CommandSender sender, @NotNull Location loc) {
        pos2 = loc;
        AudienceUtil.sendMessage(sender, Component.translatable("messages.spawn-elytra.pos2-set"));
        tryCreateZone(sender);
    }

    private void tryCreateZone(@Nullable CommandSender sender) {
        if (pos1 == null || pos2 == null) return;

        if (!Objects.equals(pos1.getWorld(), pos2.getWorld())) {
            if (sender != null)
                AudienceUtil.sendMessage(sender, Component.translatable("messages.spawn-elytra.error.different-worlds"));
            return;
        }

        boundingBox = BoundingBox.of(pos1.toVector(), pos2.toVector());
        world = pos1.getWorld();
        saveZoneToConfig();
        if (sender != null)
            AudienceUtil.sendMessage(sender, Component.translatable("messages.spawn-elytra.success"));
    }

    public void registerZone(@NotNull Location pos1, @NotNull Location pos2, @NotNull World world) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.world = world;

        boundingBox = BoundingBox.of(pos1.toVector(), pos2.toVector());
        saveZoneToConfig();
    }

    public boolean isInZone(@NotNull Player player) {
        return isInZone(player.getLocation());
    }

    public boolean isInZone(@NotNull Location location) {
        if (boundingBox == null || world == null) return false;
        return world.equals(location.getWorld()) &&
                boundingBox.contains(location.toVector());
    }

    private void saveZoneToConfig() {
        config.saveZone(
                world.getKey().asString(),
                pos1.getX(), pos1.getY(), pos1.getZ(),
                pos2.getX(), pos2.getY(), pos2.getZ()
        );
    }

    public void loadZoneFromConfig() {
        String rawWorldKey = config.zoneWorld;

        if (rawWorldKey == null || rawWorldKey.isEmpty()) {
            CoreMain.plugin.getLogger().warning("ElytraZoneManager: World key is missing in elytra-zone.yml");
            return;
        }

        Key worldKey;
        try {
            //noinspection PatternValidation
            worldKey = Key.key(rawWorldKey);
        } catch (InvalidKeyException e) {
            CoreMain.plugin.getLogger().warning("ElytraZoneManager: World key '" + rawWorldKey + "' is invalid in elytra-zone.yml");
            return;
        }

        world = Bukkit.getWorld(worldKey);
        if (world == null) {
            CoreMain.plugin.getLogger().warning("ElytraZoneManager: World '" + worldKey + "' not found.");
            return;
        }

        pos1 = new Location(world, config.zoneX1, config.zoneY1, config.zoneZ1);
        pos2 = new Location(world, config.zoneX2, config.zoneY2, config.zoneZ2);
        boundingBox = BoundingBox.of(pos1.toVector(), pos2.toVector());
    }
}