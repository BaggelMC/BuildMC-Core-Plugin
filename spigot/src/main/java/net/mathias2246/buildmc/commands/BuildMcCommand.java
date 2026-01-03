package net.mathias2246.buildmc.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.Main;
import net.mathias2246.buildmc.api.status.StatusInstance;
import net.mathias2246.buildmc.endEvent.EndEventCommand;
import net.mathias2246.buildmc.status.PlayerStatusUtil;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;

import java.io.IOException;

import static net.mathias2246.buildmc.CoreMain.gson;
import static net.mathias2246.buildmc.Main.plugin;
import static net.mathias2246.buildmc.Main.statusConfig;

public class BuildMcCommand implements CustomCommand {

    @Override
    public CommandAPICommand getCommand() {
        var cmd = new CommandAPICommand("buildmc");

        cmd.executes(
                (executionInfo -> {
                    executionInfo.sender().sendMessage("/buildmc <args>");
                })
        );

        var debugSub = new CommandAPICommand(
                "debug"
        );
        debugSub.executes(
                (executionInfo) -> {
                    executionInfo.sender().sendMessage("/buildmc debug <args>");
                }
        );
        debugSub.setRequirements(
                        (c) -> c.hasPermission(new Permission("buildmc.admin"))
                );

        var statusSub = new CommandAPICommand("status");
        statusSub.setRequirements(
                sender -> sender.hasPermission("buildmc.admin")
        );

        statusSub.withSubcommand(
                new CommandAPICommand("reload").executes(
                        (command) -> {
                            Bukkit.getScheduler().runTask(CoreMain.plugin, task -> {
                                Main.statusConfig.reload();

                                for (var player : Bukkit.getOnlinePlayers()) {
                                    PlayerStatusUtil.reloadPlayerStatus(player);
                                }
                            });
                        }
                )
        );

        statusSub.withSubcommand(
                new CommandAPICommand("write").withArguments(
                        new GreedyStringArgument("status_json")
                ).executes(
                        (command) -> {
                            var json = command.args().getByClass("status_json", String.class);

                            StatusInstance status = gson.fromJson(json, StatusInstance.class);

                            statusConfig.configuration.set(
                                    status.getStatusId(),
                                    status.serialize()
                            );

                            try {
                                statusConfig.save();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            return 1;
                        }
                )
        );

        statusSub.withSubcommand(
                new CommandAPICommand("remove").withArguments(
                        new StringArgument("status_id")
                ).executes(
                        command -> {

                            var status = command.args().getByClass("status_id", String.class);
                            if (status == null) return 0;
                            if (!statusConfig.configuration.contains(status)) return 0;
                            statusConfig.configuration.set(status, null);
                            try {
                                statusConfig.save();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            return 1;
                        }
                )
        );

        cmd.withSubcommand(statusSub);

        var endSub = new EndEventCommand().getCommand();
        cmd.withSubcommand(endSub);

        // Register /buildmc sub-commands
        cmd.withSubcommand(
                debugSub
        );

        var versionSub = new CommandAPICommand("version")
                .executes(command -> {
                    String version = plugin.getDescription().getVersion();
                    Component msg = Component.text("BuildMC-Core ", NamedTextColor.AQUA)
                            .append(Component.text("v" + version, NamedTextColor.GREEN));
                    CoreMain.plugin.sendMessage(command.sender(), msg);
                    return 1;
                });

        cmd.withSubcommand(versionSub);


        return cmd;
    }
}
