package net.mathias2246.buildmc.spawnElytra;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DisableBoostListener implements Listener {
    // Disables the use of rockets to boost you when using the spawn-elytra to limit the distance you can reach
    @EventHandler
    public void onFireworkUse(PlayerElytraBoostEvent event) {
        Player player = event.getPlayer();

        if (!SpawnElytraUtil.isUsingSpawnElytra(player)) return;

        event.setCancelled(true);

        Component text = Message.msg(player, "messages.spawn-elytra.firework-disabled-hint");
        player.sendActionBar(text);
    }

}
