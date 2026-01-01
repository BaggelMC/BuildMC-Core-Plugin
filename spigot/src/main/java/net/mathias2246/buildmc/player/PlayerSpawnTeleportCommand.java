package net.mathias2246.buildmc.player;

import dev.jorel.commandapi.CommandAPICommand;
import net.mathias2246.buildmc.commands.CustomCommand;
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

                    return  TeleportTimer.teleportCommandLogic(player);
                }
        );

        return cmd;
    }
}
