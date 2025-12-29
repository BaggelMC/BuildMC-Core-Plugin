package net.mathias2246.buildmc.claims;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.api.item.AbstractCustomItem;
import net.mathias2246.buildmc.claims.claimSubCommands.CreateClaimSubCommand;
import net.mathias2246.buildmc.claims.claimSubCommands.WhitelistSubCommand;
import net.mathias2246.buildmc.claims.tool.ClaimToolItemMetaModifier;
import net.mathias2246.buildmc.claims.tools.ClaimSelectionTool;
import net.mathias2246.buildmc.commands.CustomCommand;
import net.mathias2246.buildmc.commands.claim.ClaimProtections;
import net.mathias2246.buildmc.commands.claim.ClaimRemove;
import net.mathias2246.buildmc.commands.claim.ClaimSuggestions;
import net.mathias2246.buildmc.commands.claim.ClaimWho;
import net.mathias2246.buildmc.ui.claims.ClaimSelectMenu;
import net.mathias2246.buildmc.util.CommandUtil;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static net.mathias2246.buildmc.Main.config;
import static net.mathias2246.buildmc.commands.CommandUtil.requiresPlayer;

public class ClaimCommand implements CustomCommand {

    private static final @NotNull ClaimToolItemMetaModifier claimToolNameAndTooltip = new ClaimToolItemMetaModifier();
    private static ClaimSelectionTool claimTool;


    @Override
    public LiteralCommandNode<CommandSourceStack> getCommand() {
        
        var cmd = Commands.literal("claim");

        cmd.requires(
                (command) -> {
                    boolean t = config.getBoolean("claims.enabled", true);
                    if (!t) claimTool = null;
                    else claimTool = (ClaimSelectionTool) AbstractCustomItem.customItemsRegistry.get(Objects.requireNonNull(NamespacedKey.fromString("buildmc:claim_tool")).key());
                    return t;
                }
       );

        cmd.executes(ClaimCommand::handleEdit);
        cmd.then(
                Commands.literal("claimtool")
                        .requires((command) -> config.getBoolean("claims.tool.enable-give-command", true))
                        .executes(ClaimCommand::handleGetClaimTool)
            );
        cmd.then(
                Commands.literal("edit").executes(ClaimCommand::handleEdit)
        );
        cmd.then(
                Commands.literal("who")
                        .executes(ClaimCommand::handleWho)
                        .then(
                                Commands.argument("position", ArgumentTypes.blockPosition())
                                        .executes(ClaimCommand::handleWhoAt)
                        )

        );

        cmd.then(
                Commands.literal("help").executes(ClaimCommand::handleHelp)
        );

        cmd.then(
                CreateClaimSubCommand.createSubCommand()
        );

        cmd.then(
                Commands.literal("remove")
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
                                        .executes(command -> {
                                            var sender = command.getSource().getSender();
                                            if (!(requiresPlayer(sender) instanceof Player player)) return 0;

                                            String type = StringArgumentType.getString(command, "type").toLowerCase();
                                            String claimName = StringArgumentType.getString(command, "claim");

                                            return ClaimRemove.removeClaimCommand(player, type, claimName);
                                        })
                                )
                        )
        );

        cmd.then(
                WhitelistSubCommand.whitelistSubCommand()
        );
        cmd.then(
                Commands.literal("protections")
                        .requires((command) -> !CoreMain.plugin.getConfig().getBoolean("claims.hide-all-protections"))
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
                                        .then(Commands.argument("key", ArgumentTypes.namespacedKey())
                                                .suggests((ctx, builder) -> {
                                                    String remaining = builder.getRemaining();
                                                    for (Protection flag : CoreMain.protectionsRegistry) {
                                                        String s = flag.getKey().toString();
                                                        if (!flag.isHidden() && s.startsWith(remaining.toLowerCase())) {
                                                            builder.suggest(s);
                                                        }
                                                    }
                                                    return builder.buildFuture();
                                                })
                                                .then(Commands.argument("value", StringArgumentType.word())
                                                        .suggests(CommandUtil::booleanSuggestion)
                                                        .executes(command -> {
                                                            var sender = command.getSource().getSender();
                                                            if (!(requiresPlayer(sender) instanceof Player player)) return 0;

                                                            NamespacedKey flag;

                                                            String type = StringArgumentType.getString(command, "type").toLowerCase();
                                                            String claimName = StringArgumentType.getString(command, "claim");
                                                            flag = command.getArgument("key", NamespacedKey.class);
                                                            String valueStr = StringArgumentType.getString(command, "value").toLowerCase();

                                                            if (flag == null || Protection.isHiddenProtection(CoreMain.protectionsRegistry, flag)) {
                                                                player.sendMessage(Component.translatable("messages.claims.protections.invalid-flag"));
                                                                return 0;
                                                            }

                                                            return ClaimProtections.changeClaimProtections(player, flag, valueStr, type, claimName);
                                                        })
                                                )
                                        )
                                )
                        )
        );

        return cmd.build();
    }

    protected static int handleEdit(CommandContext<CommandSourceStack> command) {
        if (!(requiresPlayer(command.getSource().getSender()) instanceof Player player)) return 0;

        ClaimSelectMenu.open(player);
        return 1;
    }
    
    protected static int handleGetClaimTool(CommandContext<CommandSourceStack> command) {
        if (!(requiresPlayer(command.getSource().getSender()) instanceof Player player)) return 0;

        // Check if there is space left in the inventory
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(Component.translatable("messages.claims.tool.full-inventory"));
            return 0;
        }

        claimTool.giveToPlayer(player, claimToolNameAndTooltip);
        player.sendMessage(Component.translatable("messages.claims.tool.give-success"));
        return 1;
    }
    
    protected static int handleWho(CommandContext<CommandSourceStack> command) {
        if (!(requiresPlayer(command.getSource().getSender()) instanceof Player player)) return 0;
        return ClaimWho.whoClaimCommand(player, player.getLocation());
    }

    protected static int handleWhoAt(CommandContext<CommandSourceStack> command) {
        if (!(requiresPlayer(command.getSource().getSender()) instanceof Player player)) return 0;

        Location l;
        try {
            l = command.getArgument("position", BlockPositionResolver.class).resolve(command.getSource()).toLocation(player.getWorld());
        } catch (CommandSyntaxException e) {
            player.sendMessage(Component.translatable("messages.error.general"));
            return 0;
        }

        return ClaimWho.whoClaimCommand(player, l);
    }
    
    protected static int handleHelp(CommandContext<CommandSourceStack> command) {
        var sender = command.getSource().getSender();
        sender.sendMessage(Component.translatable("messages.claims.help-message"));
        return 1;
    }
}
