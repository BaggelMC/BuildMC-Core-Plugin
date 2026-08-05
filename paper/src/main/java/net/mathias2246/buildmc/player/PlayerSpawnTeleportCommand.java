package net.mathias2246.buildmc.player;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.commands.CustomCommand;
import org.bukkit.entity.Player;

import static net.mathias2246.buildmc.commands.CommandUtil.requiresPlayer;

public class PlayerSpawnTeleportCommand implements CustomCommand {

    private static boolean isEnabled = true;

    @Override
    public LiteralCommandNode<CommandSourceStack> getCommand() {

        isEnabled = CoreMain.plugin.getConfig().getBoolean("spawn-teleport.enabled", true);

        var cmd = Commands.literal("spawn");
        cmd.requires(
                (command) -> isEnabled && command.getSender().hasPermission("buildmc.allow-spawn-teleport")
        );

        cmd.executes(
                (command) -> {
                    Player player = requiresPlayer(command.getSource().getSender());
                    if (player == null) return 0;

                    return TeleportTimer.teleportCommandLogic(player);
                }
        );

        return cmd.build();
    }

}
