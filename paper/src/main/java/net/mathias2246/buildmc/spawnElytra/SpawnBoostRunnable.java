package net.mathias2246.buildmc.spawnElytra;

import org.bukkit.entity.Player;

import static net.mathias2246.buildmc.Main.config;

public class SpawnBoostRunnable {

    public final int multiplyValue;

    public final Player player;

    public SpawnBoostRunnable(Player player) {
        this.player = player;

        this.multiplyValue = config.getInt("spawn-elytra.strength", 2);
        boolean boostEnabled = config.getBoolean("spawn-elytra.enabled", true);
    }


}
