package net.mathias2246.buildmc.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.key.Key;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.permissions.PermissionGroupManager;
import net.mathias2246.buildmc.api.permissions.PermissionGroup;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class PermsCommand implements CustomCommand {

    @Override
    public LiteralCommandNode<CommandSourceStack> getCommand() {

        var root = Commands.literal("perms");

        var group = Commands.literal("group");

        group.then(
                Commands.literal("list")
                        .executes(ctx -> {
                            var sender = ctx.getSource().getSender();
                            String groups = CoreMain.permissionGroupRegistry.stream()
                                    .map(PermissionGroup::getId)
                                    .collect(Collectors.joining(", "));
                            sender.sendMessage("Groups: " + groups);
                            return 1;
                        })
        );

        group.then(
                Commands.literal("assign")
                        .then(
                                Commands.argument("player", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            Bukkit.getOnlinePlayers().forEach(p -> builder.suggest(p.getName()));
                                            return builder.buildFuture();
                                        })
                                        .then(
                                                Commands.argument("group", StringArgumentType.word())
                                                        .suggests((ctx, builder) -> {
                                                            // suggest all groups
                                                            CoreMain.permissionGroupRegistry.stream()
                                                                    .map(PermissionGroup::getId)
                                                                    .forEach(builder::suggest);
                                                            return builder.buildFuture();
                                                        })
                                                        .executes(ctx -> {
                                                            var sender = ctx.getSource().getSender();
                                                            String playerName = StringArgumentType.getString(ctx, "player");
                                                            String groupId = StringArgumentType.getString(ctx, "group");

                                                            Player target = Bukkit.getPlayerExact(playerName);
                                                            if (target == null) {
                                                                sender.sendMessage("Player not found.");
                                                                return 0;
                                                            }

                                                            NamespacedKey key = PermissionGroupManager.idToNamespacedKey(groupId);
                                                            if (key == null || CoreMain.permissionGroupRegistry.get((Key) key) == null) {
                                                                sender.sendMessage("Group does not exist.");
                                                                return 0;
                                                            }

                                                            PermissionGroupManager.assignGroup(target, groupId);
                                                            PermissionGroupManager.recalculatePermissions(target);

                                                            sender.sendMessage("Assigned group '" + groupId + "' to " + target.getName());
                                                            return 1;
                                                        })
                                        )
                        )
        );

        group.then(
                Commands.literal("remove")
                        .then(
                                Commands.argument("player", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            Bukkit.getOnlinePlayers().forEach(p -> builder.suggest(p.getName()));
                                            return builder.buildFuture();
                                        })
                                        .then(
                                                Commands.argument("group", StringArgumentType.word())
                                                        .suggests((ctx, builder) -> {
                                                            String playerName = StringArgumentType.getString(ctx, "player");
                                                            Player target = Bukkit.getPlayerExact(playerName);
                                                            if (target != null) {
                                                                List<String> assigned = PermissionGroupManager.getAssignedGroups(target);
                                                                assigned.forEach(builder::suggest);
                                                            }
                                                            return builder.buildFuture();
                                                        })
                                                        .executes(ctx -> {
                                                            var sender = ctx.getSource().getSender();
                                                            String playerName = StringArgumentType.getString(ctx, "player");
                                                            String groupId = StringArgumentType.getString(ctx, "group");

                                                            Player target = Bukkit.getPlayerExact(playerName);
                                                            if (target == null) {
                                                                sender.sendMessage("Player not found.");
                                                                return 0;
                                                            }

                                                            if (!PermissionGroupManager.hasGroup(target, groupId)) {
                                                                sender.sendMessage(target.getName() + " does not have group '" + groupId + "'");
                                                                return 0;
                                                            }

                                                            PermissionGroupManager.removeGroup(target, groupId);
                                                            PermissionGroupManager.recalculatePermissions(target);

                                                            sender.sendMessage("Removed group '" + groupId + "' from " + target.getName());
                                                            return 1;
                                                        })
                                        )
                        )
        );

        root.then(group);

        var debug = Commands.literal("debug")
                .requires(cs -> cs.getSender().hasPermission("buildmc.debug"));

        debug.then(
                Commands.literal("showperms")
                        .then(
                                Commands.argument("player", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            Bukkit.getOnlinePlayers().forEach(p -> builder.suggest(p.getName()));
                                            return builder.buildFuture();
                                        })
                                        .executes(ctx -> {
                                            var sender = ctx.getSource().getSender();
                                            String playerName = StringArgumentType.getString(ctx, "player");
                                            Player target = Bukkit.getPlayerExact(playerName);

                                            if (target == null) {
                                                sender.sendMessage("Player not found.");
                                                return 0;
                                            }

                                            sender.sendMessage("Permissions for " + target.getName() + ":");

                                            target.getEffectivePermissions().forEach(info -> {
                                                String perm = info.getPermission();
                                                boolean value = info.getValue();
                                                sender.sendMessage("  " + perm + " = " + value);
                                            });

                                            return 1;
                                        })
                        )
        );


        root.then(debug);

        return root.build();
    }
}
