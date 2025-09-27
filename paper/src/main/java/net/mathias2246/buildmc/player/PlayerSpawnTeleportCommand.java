package net.mathias2246.buildmc.player;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.event.player.PlayerSpawnTeleportPreConditionEvent;
import net.mathias2246.buildmc.commands.CustomCommand;
import net.mathias2246.buildmc.util.CommandUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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

        return cmd.build();
    }

}
