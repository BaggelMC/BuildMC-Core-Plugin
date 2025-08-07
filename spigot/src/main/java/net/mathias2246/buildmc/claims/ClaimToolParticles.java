package net.mathias2246.buildmc.claims;

import net.mathias2246.buildmc.util.ParticleSpawner;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ClaimToolParticles extends ParticleSpawner {
    public ClaimToolParticles(int repeatTimes, int delay, @NotNull Player source) {
        super(repeatTimes, delay, source);
    }

    public static class Builder implements ParticleSpawner.Builder<ClaimToolParticles> {

        @Override
        public ClaimToolParticles build(@NotNull Player player) {

            return new ClaimToolParticles(24, 5, player);
        }
    }

    @Override
    public void buildParticleSpawner() {

    }

    @Override
    protected boolean shouldStop() {
        return false;
    }

    @Override
    protected void display() {

    }

    @Override
    protected void onStop() {

    }
}
