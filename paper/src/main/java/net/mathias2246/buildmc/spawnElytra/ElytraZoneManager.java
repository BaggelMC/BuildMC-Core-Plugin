package net.mathias2246.buildmc.spawnElytra;

import net.mathias2246.buildmc.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

import static net.mathias2246.buildmc.Main.*;

/**Class for storing and managing the spawn-elytra zone*/
public class ElytraZoneManager {

    private Location pos1;
    private Location pos2;
    private BoundingBox boundingBox;
    private World world;

    public void setPos1(@NotNull Player player, @NotNull Location loc) {
        pos1 = loc;
        player.sendMessage(Message.msg(player, "messages.spawn-elytra.pos1-set"));
        tryCreateZone(player);
    }

    public void setPos2(@NotNull Player player, @NotNull Location loc) {
        pos2 = loc;
        player.sendMessage(Message.msg(player, "messages.spawn-elytra.pos2-set"));
        tryCreateZone(player);
    }

    private void tryCreateZone(@NotNull Player player) {
        if (pos1 == null || pos2 == null) return;

        if (!Objects.equals(pos1.getWorld(), pos2.getWorld())) {
            player.sendMessage(Message.msg(player, "messages.spawn-elytra.error.different-worlds"));
            return;
        }

        boundingBox = BoundingBox.of(pos1.toVector(), pos2.toVector());
        world = pos1.getWorld();
        saveZoneToConfig();
        player.sendMessage(Message.msg(player, "messages.spawn-elytra.success"));
    }

    public boolean isInZone(@NotNull Player player) {
        if (boundingBox == null || world == null) return false;
        Location loc = player.getLocation();
        return world.equals(loc.getWorld()) &&
                boundingBox.contains(loc.toVector());
    }

    private void saveZoneToConfig() {
        config.set("spawn-elytra.zone.world", world.getName());
        config.set("spawn-elytra.zone.pos1.x", pos1.getX());
        config.set("spawn-elytra.zone.pos1.y", pos1.getY());
        config.set("spawn-elytra.zone.pos1.z", pos1.getZ());
        config.set("spawn-elytra.zone.pos2.x", pos2.getX());
        config.set("spawn-elytra.zone.pos2.y", pos2.getY());
        config.set("spawn-elytra.zone.pos2.z", pos2.getZ());

        try {
            config.save(configFile);
        } catch (IOException e) {
            logger.warning("Failed to save config file: " + e.getMessage());
        }
    }

    public void loadZoneFromConfig() {
        String worldName = config.getString("spawn-elytra.zone.world");
        if (worldName == null) return;

        world = Bukkit.getWorld(worldName);
        if (world == null) {
            logger.warning("ElytraZoneManager: World '" + worldName + "' not found.");
            return;
        }

        try {
            double x1 = config.getDouble("spawn-elytra.zone.pos1.x");
            double y1 = config.getDouble("spawn-elytra.zone.pos1.y");
            double z1 = config.getDouble("spawn-elytra.zone.pos1.z");

            double x2 = config.getDouble("spawn-elytra.zone.pos2.x");
            double y2 = config.getDouble("spawn-elytra.zone.pos2.y");
            double z2 = config.getDouble("spawn-elytra.zone.pos2.z");

            pos1 = new Location(world, x1, y1, z1);
            pos2 = new Location(world, x2, y2, z2);
            boundingBox = BoundingBox.of(pos1.toVector(), pos2.toVector());
        } catch (Exception e) {
            logger.warning("ElytraZoneManager: Failed to load elytra zone from config: " + e.getMessage());
        }

    }

}
