package net.mathias2246.buildmc.spawnElytra;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import static net.mathias2246.buildmc.Main.zoneManager;

public class ElytraZoneCommand {


    @SuppressWarnings("UnstableApiUsage") // Because BlockPosition is experimental
    public LiteralArgumentBuilder<CommandSourceStack> getSubCommand() {

        var cmd = Commands.literal("elytrazone");
        cmd.requires((command) -> command.getSender().hasPermission("buildmc.admin"));

        var setup = Commands.literal("setup");

                var pos1 = Commands.literal("pos1");
                pos1.then(Commands.argument("loc", ArgumentTypes.blockPosition())
                                        .executes((command) -> {

                                                    if (!(command.getSource().getExecutor() instanceof Entity entity)) return 0;
                                                    BlockPositionResolver res = command.getArgument("loc", BlockPositionResolver.class);
                                                    var loc = res.resolve(command.getSource());
                                                    zoneManager.setPos1(entity, new Location(entity.getWorld(), loc.x(), loc.y(), loc.z()));
                                                    return 1;
                                                })
                                        );
                var pos2 = Commands.literal("pos2");
                pos2.then(Commands.argument("loc", ArgumentTypes.blockPosition())
                    .executes((command) -> {

                                if (!(command.getSource().getExecutor() instanceof Entity entity)) return 0;
                                BlockPositionResolver res = command.getArgument("loc", BlockPositionResolver.class);
                                var loc = res.resolve(command.getSource());
                                zoneManager.setPos2(entity, new Location(entity.getWorld(), loc.x(), loc.y(), loc.z()));
                                return 1;
                            })
                );

                var worldCmd = Commands.literal("world");
                worldCmd.then(
                        Commands.argument("world", ArgumentTypes.world())
                                .executes(
                                        (command) -> {
                                            World world = command.getArgument("world", World.class);
                                            zoneManager.setWorld(world);
                                            command.getSource().getSender().sendMessage("Changed world of elytra-zone successfully.");
                                            return 1;
                                        }
                                )
                );


                setup.then(pos1);
                setup.then(pos2);
                setup.then(worldCmd);

                cmd.then(setup);

        return cmd;
    }
}
