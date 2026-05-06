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

import java.io.IOException;
import java.util.Objects;

/**Class for storing and managing the spawn-elytra zone*/
public class ElytraZoneManager {

    private Location pos1;
    private Location pos2;
    private BoundingBox boundingBox;

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

    private World world;

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
            if (sender != null) AudienceUtil.sendMessage(sender, Component.translatable("messages.spawn-elytra.error.different-worlds"));
            return;
        }

        boundingBox = BoundingBox.of(pos1.toVector(), pos2.toVector());
        world = pos1.getWorld();
        saveZoneToConfig();
        if (sender != null) AudienceUtil.sendMessage(sender, Component.translatable("messages.spawn-elytra.success"));
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
        CoreMain.config.set("spawn-elytra.zone.world", world.getKey());
        CoreMain.config.set("spawn-elytra.zone.pos1.x", pos1.getX());
        CoreMain.config.set("spawn-elytra.zone.pos1.y", pos1.getY());
        CoreMain.config.set("spawn-elytra.zone.pos1.z", pos1.getZ());
        CoreMain.config.set("spawn-elytra.zone.pos2.x", pos2.getX());
        CoreMain.config.set("spawn-elytra.zone.pos2.y", pos2.getY());
        CoreMain.config.set("spawn-elytra.zone.pos2.z", pos2.getZ());

        try {
            CoreMain.config.save(CoreMain.configFile);
        } catch (IOException e) {
            CoreMain.plugin.getLogger().warning("Failed to save config file while storing elytra-zone because of exception: " + e.getMessage());
        }
    }

    public void loadZoneFromConfig() {
        Key worldKey;

        try {
            //noinspection PatternValidation
            worldKey = Key.key(Objects.requireNonNull(CoreMain.config.getString("spawn-elytra.zone.world")));
        } catch (InvalidKeyException ignore) {
            CoreMain.plugin.getLogger().warning("ElytraZoneManager: World key seems to be invalid inside the config");
            return;
        }  catch (NullPointerException ignore) {
            CoreMain.plugin.getLogger().warning("ElytraZoneManager: World key seems to be missing inside the config");
            return;
        }

        world = Bukkit.getWorld(worldKey);
        if (world == null) {
            CoreMain.plugin.getLogger().warning("ElytraZoneManager: World with key '" + worldKey + "' not found.");
            return;
        }

        double x1 = CoreMain.config.getDouble("spawn-elytra.zone.pos1.x");
        double y1 = CoreMain.config.getDouble("spawn-elytra.zone.pos1.y");
        double z1 = CoreMain.config.getDouble("spawn-elytra.zone.pos1.z");

        double x2 = CoreMain.config.getDouble("spawn-elytra.zone.pos2.x");
        double y2 = CoreMain.config.getDouble("spawn-elytra.zone.pos2.y");
        double z2 = CoreMain.config.getDouble("spawn-elytra.zone.pos2.z");

        pos1 = new Location(world, x1, y1, z1);
        pos2 = new Location(world, x2, y2, z2);
        boundingBox = BoundingBox.of(pos1.toVector(), pos2.toVector());

    }

}
