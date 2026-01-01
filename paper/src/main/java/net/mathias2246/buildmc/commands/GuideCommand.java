package net.mathias2246.buildmc.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

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
                                (ctx, builder) -> GuidesCommand.buildSuggestions(builder)
                        )
        );

        return cmd.build();
    }

}

