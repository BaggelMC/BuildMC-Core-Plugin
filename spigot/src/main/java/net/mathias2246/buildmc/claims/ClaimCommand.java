package net.mathias2246.buildmc.claims;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.NamespacedKeyArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.api.item.AbstractCustomItem;
import net.mathias2246.buildmc.claims.tool.ClaimToolItemMetaModifier;
import net.mathias2246.buildmc.claims.tools.ClaimSelectionTool;
import net.mathias2246.buildmc.commands.CustomCommand;
import net.mathias2246.buildmc.commands.claim.*;
import net.mathias2246.buildmc.ui.claims.ClaimSelectMenu;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

import static net.mathias2246.buildmc.Main.audiences;
import static net.mathias2246.buildmc.Main.config;
import static net.mathias2246.buildmc.commands.CommandUtil.requiresPlayer;

public class ClaimCommand implements CustomCommand {
    @Override
    public CommandAPICommand getCommand() {
        ClaimToolItemMetaModifier modifier = new ClaimToolItemMetaModifier();
        ClaimSelectionTool claimTool = (ClaimSelectionTool) Objects.requireNonNull(AbstractCustomItem.customItemsRegistry.get(Objects.requireNonNull(NamespacedKey.fromString("buildmc:claim_tool"))));
        boolean isClaimtoolGiveEnabled =  config.getBoolean("claims.tool.enable-give-command", true);

        return new CommandAPICommand("claim")

                .executes(
                        (command) -> {
                            if (!(requiresPlayer(command.sender()) instanceof Player player)) return;

                            ClaimSelectMenu.open(player);
                        })

                .withSubcommand(
                    new CommandAPICommand("claimtool")
                            .withRequirement(
                                    (cmd) -> isClaimtoolGiveEnabled
                            )
                    .executes(
                            (command) -> {
                                if (!(requiresPlayer(command.sender()) instanceof Player player)) return;

                                // Check if there is space left in the inventory
                                if (player.getInventory().firstEmpty() == -1) {
                                    audiences.sender(player).sendMessage(Message.msg(player, "messages.claims.tool.full-inventory"));

                                    return;
                                }

                                claimTool.giveToPlayer(player, modifier);
                                audiences.sender(player).sendMessage(Message.msg(player, "messages.claims.tool.give-success"));
                            })
                )

                .withSubcommand(
                        new CommandAPICommand("edit")
                                .executes(
                                        (command) -> {
                                            if (!(requiresPlayer(command.sender()) instanceof Player player)) return;

                                            ClaimSelectMenu.open(player);
                                        })
                )
                .withSubcommand(
                        new CommandAPICommand("who")
                                .executes(
                                        (command) -> {
                                            if (!(requiresPlayer(command.sender()) instanceof Player player)) return 0;

                                            return ClaimWho.whoClaimCommand(player, player.getLocation());
                                        }
                                )
                                .withArguments(
                                        new LocationArgument("loc")
                                                .executes(
                                                        (command) -> {
                                                            if (!(requiresPlayer(command.sender()) instanceof Player player)) return 0;

                                                            Claim claim;

                                                            Location l = command.args().getByClass("loc", Location.class);
                                                            if (l == null) {
                                                                CoreMain.plugin.sendMessage(player, Component.translatable("messages.error.general"));
                                                                return 0;
                                                            }

                                                            return ClaimWho.whoClaimCommand(player, l);
                                                        }
                                                )
                                )
                )

                .withSubcommand(
                        new CommandAPICommand("help")
                                .executes((command) -> {
                                    CommandSender sender = command.sender();
                                    audiences.sender(sender).sendMessage(Component.translatable("messages.claims.help-message"));
                                })
                )

                .withSubcommand(
                        new CommandAPICommand("create")
                                .withArguments(
                                        new StringArgument("type")
                                                .replaceSuggestions((ctx, builder) -> {
                                                    if (ctx.sender() instanceof Player player)
                                                        return ClaimSuggestions.claimTypesSuggestions(player, builder);
                                                    return builder.buildFuture();
                                                })
                                )
                                .withArguments(new StringArgument("name"))
                                .executes((command) -> {
                                    if (!(requiresPlayer(command.sender()) instanceof Player player)) return 0;

                                    String type = command.args().getByClass("type", String.class);
                                    String name = command.args().getByClass("name", String.class);

                                    if (type == null || name == null) {
                                        audiences.player(player).sendMessage(Component.translatable("messages.error.invalid-args"));
                                        return 0;
                                    }

                                    return ClaimCreate.createClaimCommand(player, type, name);
                                })
                )

                .withSubcommand(
                        new CommandAPICommand("remove")
                                .withArguments(
                                        new StringArgument("type")
                                                .replaceSuggestions((ctx, builder) -> {
                                                    if (ctx.sender() instanceof Player player)
                                                        return ClaimSuggestions.claimTypesSuggestions(player, builder);
                                                    return builder.buildFuture();
                                                }),
                                        new StringArgument("claim")
                                                .replaceSuggestions(
                                                        (ctx, builder) -> {
                                                            if (ctx.sender() instanceof Player player) {
                                                                return ClaimSuggestions.claimIdsSuggestions(
                                                                        player,
                                                                        ctx.previousArgs().getByClassOrDefault("type", String.class, "player"),
                                                                        builder
                                                                );
                                                            }
                                                            return builder.buildFuture();
                                                        }
                                                )
                                )
                                .executes((command) -> {
                                    if (!(requiresPlayer(command.sender()) instanceof Player player)) return 0;

                                    String type = command.args().getByClass("type", String.class);
                                    String claimName = command.args().getByClass("claim", String.class);

                                    if (type == null || claimName == null) {
                                        audiences.player(player).sendMessage(Component.translatable("messages.error.invalid-args"));
                                        return 0;
                                    }

                                    return ClaimRemove.removeClaimCommand(player, type, claimName);
                                })
                )

                .withSubcommand(
                        new CommandAPICommand("whitelist")
                                .withArguments(
                                        new StringArgument("action").replaceSuggestions((info, builder) -> builder.suggest("add").suggest("remove").buildFuture()),

                                        new StringArgument("type")
                                                .replaceSuggestions((ctx, builder) -> {
                                                    if (ctx.sender() instanceof Player player)
                                                        return ClaimSuggestions.claimTypesSuggestions(player, builder);
                                                    return builder.buildFuture();
                                                }),

                                        new StringArgument("claim")
                                                .replaceSuggestions(
                                                        (ctx, builder) -> {
                                                            if (ctx.sender() instanceof Player player) {
                                                                return ClaimSuggestions.claimIdsSuggestions(
                                                                        player,
                                                                        ctx.previousArgs().getByClassOrDefault("type", String.class, "player"),
                                                                        builder
                                                                );
                                                            }
                                                            return builder.buildFuture();
                                                        }
                                                ),

                                        new StringArgument("targetPlayer")
                                                .replaceSuggestions((info, builder) -> {
                                                    CommandSender sender = info.sender();
                                                    if (!(sender instanceof Player player))
                                                        return builder.buildFuture();
                                                    List<String> inputArgs = List.of(info.currentInput().split(" "));
                                                    if (inputArgs.size() < 4)
                                                        return builder.buildFuture();
                                                    String type = inputArgs.get(2);
                                                    String claimName = inputArgs.get(3);

                                                    return ClaimSuggestions.claimPlayerWhitelistSuggestions(player, type, claimName, builder);
                                                })

                                )
                                .executes((command) -> {
                                    if (!(requiresPlayer(command.sender()) instanceof Player player)) return 0;

                                    String action = command.args().getByClass("action", String.class);
                                    String type = command.args().getByClass("type", String.class);
                                    String claimName = command.args().getByClass("claim", String.class);
                                    String targetPlayerName = command.args().getByClass("targetPlayer", String.class);

                                    if (action == null || type == null || claimName == null || targetPlayerName == null) {
                                        audiences.player(player).sendMessage(Component.translatable("messages.error.invalid-args"));
                                        return 0;
                                    }

                                    return ClaimWhitelist.whitelistClaimCommand(player, type, claimName, action, targetPlayerName);
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("protections")
                                .withArguments(
                                        new StringArgument("type")
                                                .replaceSuggestions((ctx, builder) -> {
                                                    if (ctx.sender() instanceof Player player)
                                                        return ClaimSuggestions.claimTypesSuggestions(player, builder);
                                                    return builder.buildFuture();
                                                }),
                                        new StringArgument("claim")
                                                .replaceSuggestions(
                                                        (ctx, builder) -> {
                                                            if (ctx.sender() instanceof Player player) {
                                                                return ClaimSuggestions.claimIdsSuggestions(
                                                                        player,
                                                                        ctx.previousArgs().getByClassOrDefault("type", String.class, "player"),
                                                                        builder
                                                                );
                                                            }
                                                            return builder.buildFuture();
                                                        }
                                                ),
                                        new NamespacedKeyArgument("flag")
                                                .replaceSuggestions((info, builder) -> {
                                                    String remaining = builder.getRemaining();
                                                    for (Protection flag : CoreMain.protectionsRegistry) {
                                                        String s = flag.getKey().toString();
                                                        if (!flag.isHidden() && s.startsWith(remaining.toLowerCase())) {
                                                            builder.suggest(s);
                                                        }
                                                    }
                                                    return builder.buildFuture();
                                                }),
                                        new StringArgument("value")
                                                .replaceSuggestions((info, builder) -> builder.suggest("true").suggest("false").buildFuture())
                                )
                                .executes((command) -> {
                                    if (!(requiresPlayer(command.sender()) instanceof Player player)) return 0;

                                    String type = command.args().getByClass("type", String.class);
                                    String claimName = command.args().getByClass("claim", String.class);
                                    NamespacedKey flag = command.args().getByClass("flag", NamespacedKey.class);
                                    String valueStr = command.args().getByClass("value", String.class);

                                    // Null checks
                                    if (type == null || claimName == null || flag == null || valueStr == null) {
                                        audiences.player(player).sendMessage(Component.translatable("messages.error.invalid-args"));
                                        return 0;
                                    }

                                    return ClaimProtections.changeClaimProtections(player, flag, valueStr, type, claimName);
                                })
                )



                ;
    }
}
