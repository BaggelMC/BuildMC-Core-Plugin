package net.mathias2246.buildmc.claims.claimSubCommands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.mathias2246.buildmc.commands.claim.ClaimSuggestions;
import net.mathias2246.buildmc.commands.claim.ClaimWhitelist;
import org.bukkit.entity.Player;

import java.util.List;

import static net.mathias2246.buildmc.commands.CommandUtil.requiresPlayer;

public class WhitelistSubCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> whitelistSubCommand() {
        return Commands.literal("whitelist")
                .then(Commands.argument("action", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            List<String> actions = List.of("add", "remove");
                            for (String action : actions) {
                                if (action.startsWith(builder.getRemaining())) {
                                    builder.suggest(action);
                                }
                            }
                            return builder.buildFuture();
                        })
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    if (ctx.getSource().getSender() instanceof Player player)
                                        return ClaimSuggestions.claimTypesSuggestions(player, builder);
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("claim", StringArgumentType.word())
                                        .suggests(
                                                (ctx, builder) -> {
                                                    if (ctx.getSource().getSender() instanceof Player player) {
                                                        return ClaimSuggestions.claimIdsSuggestions(player, ctx.getArgument("type", String.class), builder);
                                                    }
                                                    return builder.buildFuture();
                                                }
                                        )
                                        .then(Commands.argument("player", StringArgumentType.word())
                                                .suggests((ctx, builder) -> {
                                                    var sender = ctx.getSource().getSender();
                                                    if (!(sender instanceof Player player)) return builder.buildFuture();
                                                    String type = StringArgumentType.getString(ctx, "type");
                                                    String claimName = StringArgumentType.getString(ctx, "claim");
                                                    return ClaimSuggestions.claimPlayerWhitelistSuggestions(player, type, claimName, builder);
                                                })
                                                .executes(command -> {
                                                    var sender = command.getSource().getSender();
                                                    if (!(requiresPlayer(sender) instanceof Player player)) return 0;

                                                    String action = StringArgumentType.getString(command, "action").toLowerCase();
                                                    String type = StringArgumentType.getString(command, "type").toLowerCase();
                                                    String claimName = StringArgumentType.getString(command, "claim");
                                                    String targetPlayerName = StringArgumentType.getString(command, "player");

                                                    return ClaimWhitelist.whitelistClaimCommand(player, type, claimName, action, targetPlayerName);
                                                })
                                        )
                                )
                        )
                );
    }
}
