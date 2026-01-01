package net.mathias2246.buildmc.spawnElytra;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

/**Class for storing and managing the spawn-elytra zone*/
public class ElytraZoneManager {

    private Location pos1;
    private Location pos2;
    private BoundingBox boundingBox;
    private World world;

    public void setPos1(@NotNull Player player, @NotNull Location loc) {
        pos1 = loc;
        CoreMain.plugin.sendMessage(player, Component.translatable("messages.spawn-elytra.pos1-set"));
        tryCreateZone(player);
    }

    public void setPos2(@NotNull Player player, @NotNull Location loc) {
        pos2 = loc;
        CoreMain.plugin.sendMessage(player, Component.translatable("messages.spawn-elytra.pos2-set"));
        tryCreateZone(player);
    }

    private void tryCreateZone(@NotNull Player player) {
        if (pos1 == null || pos2 == null) return;

        if (!Objects.equals(pos1.getWorld(), pos2.getWorld())) {
            CoreMain.plugin.sendMessage(player, Component.translatable("messages.spawn-elytra.error.different-worlds"));
            return;
        }

        boundingBox = BoundingBox.of(pos1.toVector(), pos2.toVector());
        world = pos1.getWorld();
        saveZoneToConfig();
        CoreMain.plugin.sendMessage(player, Component.translatable("messages.spawn-elytra.success"));
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
        CoreMain.config.set("spawn-elytra.zone.world", world.getName());
        CoreMain.config.set("spawn-elytra.zone.pos1.x", pos1.getX());
        CoreMain.config.set("spawn-elytra.zone.pos1.y", pos1.getY());
        CoreMain.config.set("spawn-elytra.zone.pos1.z", pos1.getZ());
        CoreMain.config.set("spawn-elytra.zone.pos2.x", pos2.getX());
        CoreMain.config.set("spawn-elytra.zone.pos2.y", pos2.getY());
        CoreMain.config.set("spawn-elytra.zone.pos2.z", pos2.getZ());

        try {
            CoreMain.config.save(CoreMain.configFile);
        } catch (IOException e) {
            CoreMain.plugin.getLogger().warning("Failed to save config file: " + e.getMessage());
        }
    }

    public void loadZoneFromConfig() {
        String worldName = CoreMain.config.getString("spawn-elytra.zone.world");
        if (worldName == null) return;

        world = Bukkit.getWorld(worldName);
        if (world == null) {
            CoreMain.plugin.getLogger().warning("ElytraZoneManager: World '" + worldName + "' not found.");
            return;
        }

        try {
            double x1 = CoreMain.config.getDouble("spawn-elytra.zone.pos1.x");
            double y1 = CoreMain.config.getDouble("spawn-elytra.zone.pos1.y");
            double z1 = CoreMain.config.getDouble("spawn-elytra.zone.pos1.z");

            double x2 = CoreMain.config.getDouble("spawn-elytra.zone.pos2.x");
            double y2 = CoreMain.config.getDouble("spawn-elytra.zone.pos2.y");
            double z2 = CoreMain.config.getDouble("spawn-elytra.zone.pos2.z");

            pos1 = new Location(world, x1, y1, z1);
            pos2 = new Location(world, x2, y2, z2);
            boundingBox = BoundingBox.of(pos1.toVector(), pos2.toVector());
        } catch (Exception e) {
            CoreMain.plugin.getLogger().warning("ElytraZoneManager: Failed to load elytra zone from config: " + e.getMessage());
        }

    }

}
