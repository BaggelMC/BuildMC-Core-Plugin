package net.mathias2246.buildmc.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.mathias2246.buildmc.CoreMain;

public class GuideCommand implements CustomCommand {

    public LiteralCommandNode<CommandSourceStack> getCommand() {
        var cmd = Commands.literal("guide");

        cmd.requires((command) -> GuidesCommand.enabled);

        cmd.executes(
                command -> {
                    command.getSource().getSender().sendMessage("/guide <args>");
                    return 0;
                }
        );

        cmd.then(
                Commands.argument("guide_id", StringArgumentType.greedyString())
                        .executes(
                                command ->
                                        GuidesCommand.showGuide(
                                            command.getSource().getSender(),
                                            command.getArgument("guide_id", String.class)
                                        )
                        )
                        .suggests(
                                (ctx, builder) -> {
                                    String remaining = builder.getRemaining().toLowerCase();

                                    for (var si : CoreMain.guides.keySet()) {
                                        var statusId = si.toString().replace("buildmc:", "");
                                        if (statusId.toLowerCase().startsWith(remaining)) {
                                            builder.suggest(statusId);
                                        }
                                    }

                                    return builder.buildFuture();
                                }
                        )
        );

        return cmd.build();
    }

}

