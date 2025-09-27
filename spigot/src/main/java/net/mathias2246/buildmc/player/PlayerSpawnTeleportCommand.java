package net.mathias2246.buildmc.player;

import dev.jorel.commandapi.CommandAPICommand;
import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.event.player.PlayerSpawnTeleportPreConditionEvent;
import net.mathias2246.buildmc.commands.CustomCommand;
import net.mathias2246.buildmc.util.CommandUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerSpawnTeleportCommand implements CustomCommand {


    @Override
    public CommandAPICommand getCommand() {

        var cmd = new CommandAPICommand("spawn");
        cmd.withRequirement(
                (command) -> command.hasPermission("buildmc.allow-spawn-teleport")
        );

        cmd.executes(
                (command) -> {
                    Player player = CommandUtil.requiresPlayer(command);
                    if (player == null) return 0;

                    PlayerSpawnTeleportPreConditionEvent e = new PlayerSpawnTeleportPreConditionEvent(player, player.getWorld().getSpawnLocation());
                    Bukkit.getPluginManager().callEvent(e);
                    if (e.isCancelled()) {
                        CoreMain.mainClass.sendPlayerMessage(player, Component.translatable("messages.spawn-teleport.not-working"));
                        return 0;
                    }

                    var timer = new TeleportTimer(player, e.getTo());

                    timer.start(0);

                    return 1;
                }
        );

        return cmd;
    }
}
