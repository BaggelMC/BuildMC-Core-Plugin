package net.mathias2246.buildmc.util;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Map;

import static net.mathias2246.buildmc.CoreMain.plugin;

/** Builds a command for changing a certain permission for a player.**/
public final class PermCommandHelper {
    public static LiteralArgumentBuilder<CommandSourceStack> createPermCommand(Permission permission, @Nullable String displayAs) {
        var base = getLiteralArgumentBuilder(permission, displayAs);

        base.then(
                Commands.argument("player", ArgumentTypes.player())
                        .executes(
                                command -> {
                                    var pl = command.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(command.getSource());

                                    return playerToggle(permission, command.getSource().getSender(), pl.getFirst(), null);
                                }
                        )
                        .then(
                                Commands.argument("value", BoolArgumentType.bool())
                                        .executes(
                                                command -> {
                                                    var pl = command.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(command.getSource());
                                                    var value = command.getArgument("value", Boolean.class);
                                                    
                                                    return playerToggle(permission, command.getSource().getSender(), pl.getFirst(), value);
                                                }
                                        )
                        )
        );

        return base;
    }

    private static @NonNull LiteralArgumentBuilder<CommandSourceStack> getLiteralArgumentBuilder(Permission permission, @org.jspecify.annotations.Nullable String displayAs) {
        var base = Commands.literal(displayAs == null ? permission.getName() : displayAs);

        base.executes(
                command -> {
                    var sender = command.getSource().getSender();

                    if (!(sender instanceof Player player)) {
                        AudienceUtil.sendMessage(sender, Message.msg(sender, "messages.error.not-a-player-target"));
                        return 1;
                    }

                    return playerToggle(permission, sender, player, null);
                }
        );
        return base;
    }

    private static int playerToggle(@NotNull Permission permission, @NotNull CommandSender sender, @NotNull Player player, @Nullable Boolean value) {
        PermissionAttachment attachment = player.addAttachment(plugin);

        if (value != null) {
            attachment.setPermission(permission, value);

            AudienceUtil.sendMessage(
                    sender, Message.msg(sender, "messages.permission-change.set-success-for", Map.of("player", player.getName()))
            );

            return 1;
        }

        if (player.permissionValue(permission).toBooleanOrElse(false)) {
            attachment.setPermission(permission, false);
            AudienceUtil.sendMessage(
                    sender, Message.msg(sender, "messages.permission-change.success-disable-for", Map.of("player", player.getName()))
            );
        } else {
            attachment.setPermission(permission, true);
            AudienceUtil.sendMessage(
                    sender, Message.msg(sender, "messages.permission-change.success-for", Map.of("player", player.getName()))
            );
        }
        return 1;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> createPermCommand(@NotNull String permission, @Nullable String displayAs) {
        return createPermCommand(new Permission(permission), displayAs);
    }
}
