package net.mathias2246.buildmc.commands;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.deaths.DeathSummary;
import net.mathias2246.buildmc.deaths.DeathsCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class DeathsCommandPlatform implements CustomCommand {

    @Override
    public LiteralCommandNode<CommandSourceStack> getCommand() {

        var listCmd = Commands.literal("list")
                .requires(DeathsCommandPlatform::hasListPermission)
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            Bukkit.getOnlinePlayers()
                                    .forEach(p -> builder.suggest(p.getName()));
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            CommandSender sender = ctx.getSource().getSender();
                            String player = StringArgumentType.getString(ctx, "player");
                            return DeathsCommand.list(sender, player);
                        })
                );


        var restoreCmd = Commands.literal("restore")
                .requires(DeathsCommandPlatform::hasRestorePermission)
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            Bukkit.getOnlinePlayers()
                                    .forEach(p -> builder.suggest(p.getName()));
                            return builder.buildFuture();
                        })
                        .then(Commands.argument("id", LongArgumentType.longArg(1))
                                .suggests((ctx, builder) -> {
                                    String player = StringArgumentType.getString(ctx, "player");
                                    Player target = Bukkit.getPlayerExact(player);
                                    if (target == null) {
                                        return builder.buildFuture();
                                    }

                                    try (Connection conn = CoreMain.databaseManager.getConnection()) {
                                        List<DeathSummary> deaths =
                                                CoreMain.deathTable.getDeathsByPlayer(conn, target.getUniqueId());
                                        deaths.forEach(d -> builder.suggest(String.valueOf(d.id())));
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }

                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    CommandSender sender = ctx.getSource().getSender();
                                    String player = StringArgumentType.getString(ctx, "player");
                                    long id = LongArgumentType.getLong(ctx, "id");
                                    return DeathsCommand.restore(sender, player, id);
                                })
                        )
                );

        return Commands.literal("deaths")
                .then(listCmd)
                .then(restoreCmd)
                .build();
    }

    private static boolean hasRestorePermission(CommandSourceStack source) {
        CommandSender sender = source.getSender();
        return sender.hasPermission("buildmc.admin")
                || sender.hasPermission("buildmc.deaths.restore");
    }

    private static boolean hasListPermission(CommandSourceStack source) {
        CommandSender sender = source.getSender();
        return sender.hasPermission("buildmc.admin")
                || sender.hasPermission("buildmc.deaths.list");
    }
}
