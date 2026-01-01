package net.mathias2246.buildmc.claims.claimSubCommands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.mathias2246.buildmc.commands.claim.ClaimCreate;
import net.mathias2246.buildmc.commands.claim.ClaimSuggestions;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

import static net.mathias2246.buildmc.commands.CommandUtil.requiresPlayer;

@ApiStatus.Internal
public final class CreateClaimSubCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> createSubCommand() {
        return Commands.literal("create")
                .then(
                        Commands.argument("type", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    if (ctx.getSource().getSender() instanceof Player player)
                                        return ClaimSuggestions.claimTypesSuggestions(player, builder);
                                    return builder.buildFuture();
                                })
                                .then(
                                        Commands.argument("name", StringArgumentType.word()) // name of claim
                                                .executes(command -> {
                                                    if (!(requiresPlayer(command.getSource().getSender()) instanceof Player player)) return 0;

                                                    String type = command.getArgument("type", String.class);
                                                    String name = command.getArgument("name", String.class);

                                                    // Validate positions

                                                    return ClaimCreate.createClaimCommand(player, type, name);
                                                })
                                )
                );
    }


}
