package net.mathias2246.buildmc.player;

import dev.jorel.commandapi.CommandAPICommand;
import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.event.player.PlayerSpawnTeleportPreConditionEvent;
import net.mathias2246.buildmc.commands.CustomCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static net.mathias2246.buildmc.commands.CommandUtil.requiresPlayer;

public class PlayerSpawnTeleportCommand implements CustomCommand {


    @Override
    public CommandAPICommand getCommand() {

        var cmd = new CommandAPICommand("spawn");
        cmd.withRequirement(
                (command) -> command.hasPermission("buildmc.allow-spawn-teleport")
        );

        cmd.executes(
                (command) -> {
                    Player player = requiresPlayer(command.sender());
                    if (player == null) return 0;

                    PlayerSpawnTeleportPreConditionEvent e = new PlayerSpawnTeleportPreConditionEvent(player, player.getWorld().getSpawnLocation());
                    Bukkit.getPluginManager().callEvent(e);
                    if (e.isCancelled()) {
                        CoreMain.mainClass.sendMessage(player, Component.translatable("messages.spawn-teleport.not-working"));
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
