package net.mathias2246.buildmc.claims;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.Main;
import net.mathias2246.buildmc.commands.CustomCommand;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.io.IOException;
import java.util.Objects;

import static net.mathias2246.buildmc.Main.claimManager;

public class ClaimCommand implements CustomCommand {
    @Override
    public LiteralCommandNode<CommandSourceStack> getCommand() {
        var cmd = Commands.literal("claim");
        cmd.then(
                Commands.literal("claimtool")
                .executes(
                        (command) -> {
                            if (!(command.getSource().getExecutor() instanceof Player player)) {
                                command.getSource().getSender().sendMessage(Component.translatable("messages.error.not-a-player"));
                                return 0;
                            }
                            ClaimTool.giveToolToPlayer(player);
                            return 1;
                        })
            );
        cmd.then(
                Commands.literal("who")
                        .executes(
                                (command) -> {
                                    if (!(command.getSource().getSender() instanceof Player player)) {
                                        command.getSource().getSender().sendMessage(Component.translatable("messages.error.not-a-player"));
                                        return 0;
                                    }
                                    command.getSource().getSender().sendMessage(Objects.requireNonNull(ClaimManager.getOwnerString(command.getSource().getLocation())));
                                    return 1;
                                }
                        )
        );
        cmd.then(
                Commands.literal("help")
                        .executes(command -> {
                            var sender = command.getSource().getSender();
                            sender.sendMessage(Component.translatable("messages.claims.help-message"));
                            return 1;
                        })
        );
        cmd.then(
                Commands.literal("whitelist")
                        .then(
                                Commands.literal("add")
                                        .then(
                                                Commands.argument("player", ArgumentTypes.player())
                                                        .executes(
                                                                (command) -> {
                                                                    if (!(command.getSource().getSender() instanceof Player player)) {
                                                                        command.getSource().getSender().sendMessage(Component.translatable("messages.error.not-a-player"));
                                                                        return 0;
                                                                    }

                                                                    Team team = ClaimManager.getPlayerTeam(player);
                                                                    if (team == null) {
                                                                        command.getSource().getSender().sendMessage(Component.translatable("messages.error.not-in-a-team"));
                                                                        return 0;
                                                                    }

                                                                    var ps = command.getArgument("player", PlayerSelectorArgumentResolver.class);
                                                                    for (var p : ps.resolve(command.getSource())) {
                                                                        ClaimManager.setPlayerWhitelisted(claimManager, team, p);
                                                                        command.getSource().getSender().sendMessage(Component.translatable("messages.claims.successfully-whitelisted"));
                                                                    }
                                                                    try {
                                                                        claimManager.save();
                                                                    } catch (IOException e) {
                                                                        throw new RuntimeException(e);
                                                                    }
                                                                    return 1;
                                                                }
                                                        )
                                        )
                        )
                        .then(
                                Commands.literal("remove")
                                        .then(
                                                Commands.argument("player", ArgumentTypes.player())
                                                        .executes(
                                                                (command) -> {
                                                                    if (!(command.getSource().getSender() instanceof Player player)) {
                                                                        command.getSource().getSender().sendMessage(Component.translatable("messages.error.not-a-player"));
                                                                        return 0;
                                                                    }

                                                                    Team team = ClaimManager.getPlayerTeam(player);
                                                                    if (team == null) {
                                                                        command.getSource().getSender().sendMessage(Component.translatable("messages.error.not-in-a-team"));
                                                                        return 0;
                                                                    }

                                                                    var ps = command.getArgument("player", PlayerSelectorArgumentResolver.class);
                                                                    for (var p : ps.resolve(command.getSource())) {
                                                                        ClaimManager.removePlayerWhitelisted(claimManager, team, p);
                                                                        command.getSource().getSender().sendMessage(Component.translatable("messages.claims.successfully-removed-whitelisted"));
                                                                    }

                                                                    try {
                                                                        claimManager.save();
                                                                    } catch (IOException e) {
                                                                        throw new RuntimeException(e);
                                                                    }
                                                                    return 1;
                                                                }
                                                        )
                                        )
                        )
        );


        return cmd.build();
    }
}
