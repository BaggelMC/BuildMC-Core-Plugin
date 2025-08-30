package net.mathias2246.buildmc.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class LocationUtil {

    /**
     * Serialize a Location into a String.
     * Format: world,x,y,z,yaw,pitch
     *
     * @param location the Location to serialize
     * @return serialized String
     */
    public static String serialize(Location location) {
        if (location == null || location.getWorld() == null) {
            throw new IllegalArgumentException("Location or World cannot be null");
        }
        return location.getWorld().getName() + "," +
                location.getX() + "," +
                location.getY() + "," +
                location.getZ() + "," +
                location.getYaw() + "," +
                location.getPitch();
    }

    /**
     * Deserialize a String back into a Location.
     * Must match the format from serialize()
     *
     * @param data the serialized string
     * @return Location object
     */
    public static @NotNull Location deserialize(String data) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data string cannot be null or empty");
        }

        String[] parts = data.split(",");
        if (parts.length != 6) {
            throw new IllegalArgumentException("Invalid location format: " + data);
        }

        World world = Bukkit.getWorld(parts[0]);
        if (world == null) {
            throw new IllegalArgumentException("World not found: " + parts[0]);
        }

        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);
        float yaw = Float.parseFloat(parts[4]);
        float pitch = Float.parseFloat(parts[5]);

        return new Location(world, x, y, z, yaw, pitch);
    }

    /**
     * Deserialize a String back into a Location.
     * Must match the format from serialize()
     *
     * @param data the serialized string
     * @return Location object, or null is failed
     */
    public static @Nullable Location tryDeserialize(String data) {
        if (data == null || data.isEmpty()) {
            return null;
        }

        String[] parts = data.split(",");
        if (parts.length != 6) {
            return null;
        }

        World world = Bukkit.getWorld(parts[0]);
        if (world == null) {
            return null;
        }

        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);
        float yaw = Float.parseFloat(parts[4]);
        float pitch = Float.parseFloat(parts[5]);

        return new Location(world, x, y, z, yaw, pitch);
    }

    /**
     * Calculates the number of chunks covered by the area defined by two positions.
     * Both positions must be in the same world.
     *
     * @param pos1 First corner location
     * @param pos2 Second corner location
     * @return Number of chunks in the rectangular selection
     */
    public static int calculateChunkArea(@NotNull Location pos1, @NotNull Location pos2) {
        if (!Objects.equals(pos1.getWorld(), pos2.getWorld())) {
            throw new IllegalArgumentException("Positions are in different worlds");
        }

        int chunkX1 = pos1.getBlockX() >> 4;
        int chunkZ1 = pos1.getBlockZ() >> 4;
        int chunkX2 = pos2.getBlockX() >> 4;
        int chunkZ2 = pos2.getBlockZ() >> 4;

        return calculateChunkArea(chunkX1, chunkZ1, chunkX2, chunkZ2);
    }

    public static int calculateChunkArea(int chunkX1, int chunkZ1, int chunkX2, int chunkZ2) {
        int width = Math.abs(chunkX2 - chunkX1) + 1;
        int height = Math.abs(chunkZ2 - chunkZ1) + 1;

        return width * height;
    }
}