package net.mathias2246.buildmc.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import static net.mathias2246.buildmc.Main.zoneManager;

public class ElytraZoneCommand {


    public LiteralArgumentBuilder<CommandSourceStack> getSubCommand() {

        var cmd = Commands.literal("elytrazone");
        cmd.requires((command) -> command.getSender().hasPermission("buildmc.operator"));

        var setup = Commands.literal("setup");

                var pos1 = Commands.literal("pos1");
                pos1.then(Commands.argument("loc", ArgumentTypes.blockPosition())
                                        .executes((command) -> {

                                                    if (!(command.getSource().getExecutor() instanceof Player player)) return 0;
                                                    BlockPositionResolver res = command.getArgument("loc", BlockPositionResolver.class);
                                                    var loc = res.resolve(command.getSource());
                                                    zoneManager.setPos1(player, new Location(player.getWorld(), loc.x(), loc.y(), loc.z()));
                                                    return 1;
                                                })
                                        );
                var pos2 = Commands.literal("pos2");
                pos2.then(Commands.argument("loc", ArgumentTypes.blockPosition())
                                        .executes((command) -> {

                                                    if (!(command.getSource().getExecutor() instanceof Player player)) return 0;
                                                    BlockPositionResolver res = command.getArgument("loc", BlockPositionResolver.class);
                                                    var loc = res.resolve(command.getSource());
                                                    zoneManager.setPos2(player, new Location(player.getWorld(), loc.x(), loc.y(), loc.z()));
                                                    return 1;
                                                })
                                );

                setup.then(pos1);
                setup.then(pos2);

                cmd.then(setup);

        return cmd;
    }
}
