package net.mathias2246.buildmc.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.util.AudienceUtil;

public class GuideCommand implements CustomCommand {

    public LiteralCommandNode<CommandSourceStack> getCommand() {
        var cmd = Commands.literal("guide");

        cmd.requires((command) -> GuidesCommand.enabled);

        cmd.executes(
                command -> {
                    AudienceUtil.sendMessage(command.getSource().getSender(), Component.text("/guide <args>"));
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

