package net.mathias2246.buildmc.claims;

import com.destroystokyo.paper.ParticleBuilder;
import net.mathias2246.buildmc.Main;
import net.mathias2246.buildmc.util.LocationUtil;
import net.mathias2246.buildmc.util.ParticleSpawner;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ClaimToolParticles extends ParticleSpawner {

    public static class Builder implements ParticleSpawner.Builder<ClaimToolParticles> {

        @Override
        public ClaimToolParticles build(@NotNull Player player) {
            return new ClaimToolParticles(24, 5, player, false);
        }
    }

    public final boolean isRemoveSelection;

    public ClaimToolParticles(int repeatTimes, int delay, @NotNull Player source, boolean isRemoveSelection) {
        super(repeatTimes, delay, source);
        this.isRemoveSelection = isRemoveSelection;
        var l = LocationUtil.tryDeserialize(source.getMetadata("buildmc:claim_tool_first_selection").getFirst().asString());
        if (l == null) return;
        Chunk c = l.getChunk();

        l.setX((c.getX() << 4) + 8);
        l.setZ((c.getZ() << 4) + 8);
        l.setY(l.getY()+1.5);



        if (isRemoveSelection) {
            chunkPlane = new ParticleBuilder
                    (Particle.DUST)
                    .color(210, 10, 10)
                    .count(100)
                    .receivers(source)
                    .location(
                            l
                    )
                    .offset(2.6, 0, 2.6);
        }
        else chunkPlane = new ParticleBuilder
                (Particle.DUST)
                .color(10, 230, 10)
                .count(100)
                .receivers(source)
                .location(
                        l
                )
                .offset(2.6, 0, 2.6);

        var cornerBase = new ParticleBuilder(Particle.DUST)
                .color(10, 10, 230)
                .count(30)
                .receivers(source)
                .offset(0,1.5d, 0);

        var l1 = new Location(
                source.getWorld(),
                (source.getChunk().getX()  << 4) + 0.5d,
                l.getY(),
                (source.getChunk().getZ()  << 4) + 0.5d
        );

        corner1 = cornerBase.clone().location(
                l1
        );

        var l2 = new Location(
                source.getWorld(),
                (source.getChunk().getX()  << 4) + 15.5d,
                l.getY(),
                (source.getChunk().getZ()  << 4) + 0.0d
        );

        corner2 = cornerBase.clone().location(
                l2
        );

        var l3 = new Location(
                source.getWorld(),
                (source.getChunk().getX()  << 4) + 0.0d,
                l.getY(),
                (source.getChunk().getZ()  << 4) + 15.5d
        );

        corner3 = cornerBase.clone().location(
                l3
        );

        var l4 = new Location(
                source.getWorld(),
                (source.getChunk().getX()  << 4) + 15.5d,
                l.getY(),
                (source.getChunk().getZ()  << 4) + 15.5d
        );


        corner4 = cornerBase.clone().location(
                l4
        );
    }

    private ParticleBuilder chunkPlane;
    private ParticleBuilder corner1;
    private ParticleBuilder corner2;
    private ParticleBuilder corner3;
    private ParticleBuilder corner4;

    @Override
    public void buildParticleSpawner() {

    }

    @Override
    protected boolean shouldStop() {
        return !source.hasMetadata("claim_selection_particles");
    }

    @Override
    protected void display() {
        chunkPlane.spawn();
        corner1.spawn();
        corner2.spawn();
        corner3.spawn();
        corner4.spawn();
    }

    @Override
    protected void onStop() {
        source.removeMetadata("claim_selection_particles", Main.plugin);
    }
}
