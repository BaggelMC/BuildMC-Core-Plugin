package net.mathias2246.buildmc.spawnElytra;

import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.util.config.YamlConfigurationManager;

public class SpawnElytraConfig extends YamlConfigurationManager {

    public boolean enabled;
    public int strength;
    public boolean disableRockets;
    public boolean onJoinElytraCheck;

    public String zoneWorld;
    public double zoneX1, zoneY1, zoneZ1;
    public double zoneX2, zoneY2, zoneZ2;

    public SpawnElytraConfig() {
        super(CoreMain.plugin, "elytra-zone.yml");
    }

    @Override
    public void setupConfiguration() {
        enabled          = configuration.getBoolean("enabled", true);
        strength         = configuration.getInt("strength", 2);
        disableRockets   = configuration.getBoolean("disable-rockets", true);
        onJoinElytraCheck = configuration.getBoolean("on-join-elytra-check", true);

        zoneWorld = configuration.getString("zone.world");
        zoneX1    = configuration.getDouble("zone.pos1.x");
        zoneY1    = configuration.getDouble("zone.pos1.y");
        zoneZ1    = configuration.getDouble("zone.pos1.z");
        zoneX2    = configuration.getDouble("zone.pos2.x");
        zoneY2    = configuration.getDouble("zone.pos2.y");
        zoneZ2    = configuration.getDouble("zone.pos2.z");
    }

    @Override
    protected void preSave() {
        configuration.set("enabled",              enabled);
        configuration.set("strength",             strength);
        configuration.set("disable-rockets",      disableRockets);
        configuration.set("on-join-elytra-check", onJoinElytraCheck);
    }

    /** Persists the zone coordinates to disk. Called by ElytraZoneManager. */
    public void saveZone(String worldKey,
                         double x1, double y1, double z1,
                         double x2, double y2, double z2) {
        configuration.set("zone.world",  worldKey);
        configuration.set("zone.pos1.x", x1);
        configuration.set("zone.pos1.y", y1);
        configuration.set("zone.pos1.z", z1);
        configuration.set("zone.pos2.x", x2);
        configuration.set("zone.pos2.y", y2);
        configuration.set("zone.pos2.z", z2);

        try {
            save();
        } catch (java.io.IOException e) {
            CoreMain.plugin.getLogger().warning(
                    "Failed to save elytra-zone.yml: " + e.getMessage());
        }
    }
}