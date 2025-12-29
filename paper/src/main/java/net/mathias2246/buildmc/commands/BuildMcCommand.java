package net.mathias2246.buildmc.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.mathias2246.buildmc.api.status.StatusInstance;
import net.mathias2246.buildmc.player.status.SetStatusCommand;
import net.mathias2246.buildmc.status.PlayerStatusUtil;
import org.bukkit.Bukkit;

import java.io.IOException;

import static net.mathias2246.buildmc.CoreMain.gson;
import static net.mathias2246.buildmc.Main.statusConfig;

public class BuildMcCommand implements CustomCommand {

    @Override
    public LiteralCommandNode<CommandSourceStack> getCommand() {
        var cmd = Commands.literal("buildmc");

        cmd.executes(executionInfo -> {
            executionInfo.getSource().getSender().sendMessage("/buildmc <args>");
            return 1;
        });

        var debugSub = Commands.literal("debug")
                .requires(c -> c.getSender().hasPermission("buildmc.admin"))
                .executes(ctx -> {
                    ctx.getSource().getSender().sendMessage("/buildmc debug <args>");
                    return 1;
                });

        // --- MiniMessage debug command ---
        var miniMsgSub = Commands.literal("minimessage")
                .then(
                        Commands.argument("message", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    var sender = ctx.getSource().getSender();
                                    String input = StringArgumentType.getString(ctx, "message");

                                    MiniMessage mm = MiniMessage.miniMessage();
                                    Component component;
                                    try {
                                        component = mm.deserialize(input);
                                    } catch (Exception e) {
                                        sender.sendMessage(Component.text("❌ Failed to parse MiniMessage: " + e.getMessage(), NamedTextColor.RED));
                                        return 0;
                                    }

                                    sender.sendMessage(Component.text("Rendered Component:"));
                                    sender.sendMessage(component);

                                    String json = GsonComponentSerializer.gson().serialize(component);

                                    Component jsonCopy = Component.text("📋 Click to copy JSON", NamedTextColor.AQUA)
                                            .clickEvent(ClickEvent.copyToClipboard(json))
                                            .hoverEvent(Component.text("Copy this component's JSON to your clipboard"));

                                    sender.sendMessage(jsonCopy);
                                    return 1;
                                })
                );

        debugSub.then(miniMsgSub);
        cmd.then(debugSub);

        var statusSub =
                Commands.literal("status")
                        .requires(c -> c.getSender().hasPermission("buildmc.admin"))
                        .then(
                                Commands.literal("reload")
                                        .executes(c -> {
                                            statusConfig.reload();

                                            for (var player : Bukkit.getOnlinePlayers()) {
                                                PlayerStatusUtil.reloadPlayerStatus(player);
                                            }

                                            return 1;
                                        })
                        )
                        .then(
                                Commands.literal("remove")
                                        .then(
                                                Commands.argument("status_id", StringArgumentType.string())
                                                        .suggests(SetStatusCommand::getStatusesIdSuggestion)
                                                        .executes(
                                                                command -> {

                                                                    var status = command.getArgument("status_id", String.class);
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
                                        )
                        )
                        .then(
                                Commands.literal("write")
                                                .then(
                                                        Commands.argument("status_json", StringArgumentType.greedyString())
                                                                .executes(
                                                                        (command) -> {
                                                                            var json = command.getArgument("status_json", String.class);

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
                                                )
                        );
        cmd.then(statusSub);

        var endSub = new net.mathias2246.buildmc.endEvent.EndEventCommand().getCommandBuilder();
        cmd.then(endSub);

        var elytraSub = new net.mathias2246.buildmc.spawnElytra.ElytraZoneCommand().getSubCommand();
        cmd.then(elytraSub);

        return cmd.build();
    }
}
