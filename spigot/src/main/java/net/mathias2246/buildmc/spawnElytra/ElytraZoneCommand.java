package net.mathias2246.buildmc.spawnElytra;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import net.mathias2246.buildmc.commands.CustomCommand;
import org.bukkit.Location;

public class ElytraZoneCommand implements CustomCommand {

    private final ElytraZoneManager zoneManager;

    public ElytraZoneCommand(ElytraZoneManager zoneManager) {
        this.zoneManager = zoneManager;
    }

    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("buildmc")
                .withSubcommand(
                        new CommandAPICommand("elytrazone")
                                .withPermission("buildmc.admin")
                                .withSubcommand(new CommandAPICommand("setup")
                                        .withSubcommand(new CommandAPICommand("pos1")
                                                .withArguments(new LocationArgument("location", LocationType.PRECISE_POSITION))
                                                .executesPlayer((player, args) -> {
                                                    Location loc = args.getByClass("location", Location.class);
                                                    if (loc == null) {
                                                        // TODO: Implement error handling
                                                        return;
                                                    }
                                                    zoneManager.setPos1(player, loc);
                                                })
                                        )
                                        .withSubcommand(new CommandAPICommand("pos2")
                                                .withArguments(new LocationArgument("location", LocationType.PRECISE_POSITION))
                                                .executesPlayer((player, args) -> {
                                                    Location loc = args.getByClass("location", Location.class);
                                                    if (loc == null) {
                                                        // TODO: Implement error handling
                                                        return;
                                                    }
                                                    zoneManager.setPos2(player, loc);
                                                })
                                        )
                                )
                );
    }
}
