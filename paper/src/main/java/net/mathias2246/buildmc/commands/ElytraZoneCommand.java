package net.mathias2246.buildmc.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.mathias2246.buildmc.spawnElytra.ElytraZoneManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ElytraZoneCommand implements CustomCommand {

    private final ElytraZoneManager zoneManager;

    public ElytraZoneCommand(ElytraZoneManager zoneManager) {
        this.zoneManager = zoneManager;
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> getCommand() {
        var cmd = Commands.literal("elytrazone");
        cmd.requires((command) -> command.getSender().hasPermission("buildmc.operator"));

        var setup = Commands.literal("setup");

                var pos1 = Commands.literal("pos1");
                pos1.then(Commands.argument("loc", ArgumentTypes.blockPosition())
                                        .executes((command) -> {

                                                    if (!(command.getSource().getExecutor() instanceof Player player)) return 0;
                                                    Location loc = command.getArgument("loc", Location.class);
                                                    if (loc == null) {
                                                        // TODO: Implement error handling
                                                        return 0;
                                                    }
                                                    zoneManager.setPos1(player, loc);
                                                    return 1;
                                                })
                                        );
                var pos2 = Commands.literal("pos2");
                pos2.then(Commands.argument("loc", ArgumentTypes.blockPosition())
                                        .executes((command) -> {

                                                    if (!(command.getSource().getExecutor() instanceof Player player)) return 0;
                                                    Location loc = command.getArgument("loc", Location.class);
                                                    if (loc == null) {
                                                        // TODO: Implement error handling
                                                        return 0;
                                                    }
                                                    zoneManager.setPos2(player, loc);
                                                    return 1;
                                                })
                                );
        return cmd.build();
    }
}
