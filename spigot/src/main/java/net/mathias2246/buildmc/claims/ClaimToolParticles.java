package net.mathias2246.buildmc.claims;

import net.mathias2246.buildmc.util.LocationUtil;
import net.mathias2246.buildmc.util.ParticleSpawner;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ClaimToolParticles extends ParticleSpawner {
    public ClaimToolParticles(int repeatTimes, int delay, @NotNull Player source, boolean isRemoveSelection) {
        super(repeatTimes, delay, source);

        plane = LocationUtil.deserialize(source.getMetadata("buildmc:claim_tool_first_selection").getFirst().asString());

        Chunk c = plane.getChunk();

        plane.setX((c.getX() << 4) + 8);
        plane.setZ((c.getZ() << 4) + 8);
        plane.setY(plane.getY()+1.5);

        l1 = new Location(
                source.getWorld(),
                (source.getLocation().getChunk().getX()  << 4) + 0.5d,
                plane.getY(),
                (source.getLocation().getChunk().getZ()  << 4) + 0.5d
        );

        l2 = new Location(
                source.getWorld(),
                (source.getLocation().getChunk().getX()  << 4) + 15.5d,
                plane.getY(),
                (source.getLocation().getChunk().getZ()  << 4) + 0.0d
        );

        l3 = new Location(
                source.getWorld(),
                (source.getLocation().getChunk().getX()  << 4) + 0.0d,
                plane.getY(),
                (source.getLocation().getChunk().getZ()  << 4) + 15.5d
        );

        l4 = new Location(
                source.getWorld(),
                (source.getLocation().getChunk().getX()  << 4) + 15.5d,
                plane.getY(),
                (source.getLocation().getChunk().getZ()  << 4) + 15.5d
        );

        if (isRemoveSelection) {
            chunkPlane = new Particle.DustOptions(
                    Color.fromRGB(210, 10, 10),
                    1.0f
            );
        }
        else chunkPlane = new Particle.DustOptions(
                Color.fromRGB(10, 230, 10),
                1.0f
        );
        corner = new Particle.DustOptions(
                Color.fromRGB(10, 10, 230),
                1.0f
        );
    }

    public static class Builder implements ParticleSpawner.Builder<ClaimToolParticles> {

        @Override
        public ClaimToolParticles build(@NotNull Player player) {

            return new ClaimToolParticles(24, 5, player, false);
        }
    }

    public final Particle.DustOptions chunkPlane;
    public final Particle.DustOptions corner;

    private final Location plane;
    private final Location l1;
    private final Location l2;
    private final Location l3;
    private final Location l4;

    @Override
    public void buildParticleSpawner() {

    }

    @Override
    protected boolean shouldStop() {
        return false;
    }

    @Override
    protected void display() {
        source.spawnParticle(
                Particle.DUST,
                plane,
                100,
                2.6d, 0.0d, 2.6d,
                chunkPlane
        );
        source.spawnParticle(
                Particle.DUST,
                l1,
                100,
                0d, 1.0d, 0d,
                corner
        );
        source.spawnParticle(
                Particle.DUST,
                l2,
                100,
                0d, 1.0d, 0d,
                corner
        );
        source.spawnParticle(
                Particle.DUST,
                l3,
                100,
                0d, 1.0d, 0d,
                corner
        );
        source.spawnParticle(
                Particle.DUST,
                l4,
                100,
                0d, 1.0d, 0d,
                corner
        );
    }

    @Override
    protected void onStop() {

    }
}
