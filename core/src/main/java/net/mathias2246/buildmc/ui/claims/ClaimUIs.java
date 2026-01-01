package net.mathias2246.buildmc.ui.claims;

import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.event.claims.ClaimRemoveEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.mathias2246.buildmc.CoreMain.plugin;

public class ClaimUIs implements Listener {

    public static final Map<Long, List<Player>> openUIs = new HashMap<>();

    @EventHandler
    public static void closeUIOnDelete(ClaimRemoveEvent event) {

        Claim claim = event.getClaim();
        Long id = claim.getId();

        if (!openUIs.containsKey(id)) return;
        List<Player> players = openUIs.get(id);

        if (players == null) return;

        Bukkit.getScheduler().runTask(plugin, task -> {
            for (var player : players) {
                player.closeInventory();
            }
        });
        openUIs.remove(id);
    }

}
