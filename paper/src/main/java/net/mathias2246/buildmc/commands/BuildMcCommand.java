package net.mathias2246.buildmc.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.mathias2246.buildmc.Main;

public class BuildMcCommand implements CustomCommand {

    @Override
    public LiteralCommandNode<CommandSourceStack> getCommand() {
        var cmd = Commands.literal("buildmc");

        cmd.executes(executionInfo -> {
            executionInfo.getSource().getSender().sendMessage("/buildmc <args>");
            return 1;
        });

        var debugSub = Commands.literal("debug")
                .requires(c -> c.getSender().hasPermission("buildmc.debug"))
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
                                            Main.statusConfig.reload();
                                            return 1;
                                        })
                        );
        cmd.then(statusSub);

        var endSub = new net.mathias2246.buildmc.endEvent.EndEventCommand().getCommandBuilder();
        cmd.then(endSub);

        var elytraSub = new net.mathias2246.buildmc.spawnElytra.ElytraZoneCommand().getSubCommand();
        cmd.then(elytraSub);

        return cmd.build();
    }
}
