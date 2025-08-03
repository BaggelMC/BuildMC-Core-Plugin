package net.mathias2246.buildmc.claims;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public record ClaimDataSaveListener(@NotNull ClaimManager claimManager) implements Listener {

    @EventHandler
    public void onSave(WorldSaveEvent event) {
        if (!Bukkit.getWorlds().getFirst().getName().equals(event.getWorld().getName())) return;

        try {
            claimManager.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
