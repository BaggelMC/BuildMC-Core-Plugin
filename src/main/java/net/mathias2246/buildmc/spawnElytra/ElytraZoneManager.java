package net.mathias2246.buildmc.spawnElytra;

import net.mathias2246.buildmc.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import java.io.IOException;
import java.util.Objects;

import static net.mathias2246.buildmc.Main.*;

public class ElytraZoneManager {

    private Location pos1;
    private Location pos2;
    private BoundingBox boundingBox;
    private World world;

    public void setPos1(Player player, Location loc) {
        pos1 = loc;
        player.sendMessage("§aPosition 1 set.");
        tryCreateZone(player);
    }

    public void setPos2(Player player, Location loc) {
        pos2 = loc;
        player.sendMessage("§aPosition 2 set.");
        tryCreateZone(player);
    }

    private void tryCreateZone(Player player) {
        if (pos1 == null || pos2 == null) return;

        if (!Objects.equals(pos1.getWorld(), pos2.getWorld())) {
            player.sendMessage("§cPositions must be in the same world.");
            return;
        }

        boundingBox = BoundingBox.of(pos1.toVector(), pos2.toVector());
        world = pos1.getWorld();
        saveZoneToConfig();
        player.sendMessage("§aElytra zone created.");
    }

    public boolean isInZone(Player player) {
        if (boundingBox == null || world == null) return false;
        Location loc = player.getLocation();
        return world.equals(loc.getWorld()) &&
                boundingBox.contains(loc.getX(), loc.getY(), loc.getZ());
    }

    private void saveZoneToConfig() {
        config.set("SpawnElytra.Zone.world", world.getName());
        config.set("SpawnElytra.Zone.pos1.x", pos1.getX());
        config.set("SpawnElytra.Zone.pos1.y", pos1.getY());
        config.set("SpawnElytra.Zone.pos1.z", pos1.getZ());
        config.set("SpawnElytra.Zone.pos2.x", pos2.getX());
        config.set("SpawnElytra.Zone.pos2.y", pos2.getY());
        config.set("SpawnElytra.Zone.pos2.z", pos2.getZ());

        try {
            config.save(configFile);
        } catch (IOException e) {
            logger.warning("Failed to save config file: " + e.getMessage());
        }
    }

    public void loadZoneFromConfig() {
        String worldName = config.getString("SpawnElytra.Zone.world");
        if (worldName == null) return;

        world = Bukkit.getWorld(worldName);
        if (world == null) {
            logger.warning("ElytraZoneManager: World '" + worldName + "' not found.");
            return;
        }

        try {
            double x1 = config.getDouble("SpawnElytra.Zone.pos1.x");
            double y1 = config.getDouble("SpawnElytra.Zone.pos1.y");
            double z1 = config.getDouble("SpawnElytra.Zone.pos1.z");

            double x2 = config.getDouble("SpawnElytra.Zone.pos2.x");
            double y2 = config.getDouble("SpawnElytra.Zone.pos2.y");
            double z2 = config.getDouble("SpawnElytra.Zone.pos2.z");

            pos1 = new Location(world, x1, y1, z1);
            pos2 = new Location(world, x2, y2, z2);
            boundingBox = BoundingBox.of(pos1.toVector(), pos2.toVector());
        } catch (Exception e) {
            logger.warning("ElytraZoneManager: Failed to load elytra zone from config: " + e.getMessage());
        }

    }

}
