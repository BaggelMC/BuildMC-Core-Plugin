package net.mathias2246.buildmc.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
}