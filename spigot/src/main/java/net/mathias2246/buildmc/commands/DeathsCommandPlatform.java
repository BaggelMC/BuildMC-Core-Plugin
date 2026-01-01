package net.mathias2246.buildmc.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LongArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.deaths.DeathsCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;

public class DeathsCommandPlatform implements CustomCommand {

    @Override
    public CommandAPICommand getCommand() {
        var deathsCmd = new CommandAPICommand("deaths");

        var listCmd = new CommandAPICommand("list")
                .withArguments(new StringArgument("player")
                        .replaceSuggestions((info, builder) -> {
                            Bukkit.getOnlinePlayers().forEach(p -> builder.suggest(p.getName()));
                            return builder.buildFuture();
                        })
                )
                .executes((info) -> {
                    CommandSender sender = info.sender();
                    String playerName = info.args().getByClass("player", String.class);
                    return DeathsCommand.list(sender, playerName);
                })
                .withRequirement(sender ->
                        sender.hasPermission("buildmc.admin") ||
                                sender.hasPermission("buildmc.list-deaths")
                );

        var restoreCmd = new CommandAPICommand("restore")
                .withArguments(new StringArgument("player")
                        .replaceSuggestions((info, builder) -> {
                            Bukkit.getOnlinePlayers().forEach(p -> builder.suggest(p.getName()));
                            return builder.buildFuture();
                        })
                )
                .withArguments(new LongArgument("id", 1)
                        .replaceSuggestions((info, builder) -> {
                            String playerName = info.previousArgs().getByClass("player", String.class);
                            if (playerName == null) return builder.buildFuture();
                            Player target = Bukkit.getPlayerExact(playerName);
                            if (target == null) return builder.buildFuture();

                            try (Connection conn = CoreMain.databaseManager.getConnection()) {
                                CoreMain.deathTable.getDeathsByPlayer(conn, target.getUniqueId())
                                        .forEach(d -> builder.suggest(String.valueOf(d.id())));
                            } catch (Exception ignored) {}

                            return builder.buildFuture();
                        })
                )
                .executes((info) -> {
                    CommandSender sender = info.sender();
                    String playerName = info.args().getByClass("player", String.class);
                    Long id = info.args().getByClass("id", Long.class);
                    if (id == null) {
                        CoreMain.plugin.sendMessage(sender, Component.translatable("messages.deaths.error.invalid-id"));
                        return 0;
                    }

                    return DeathsCommand.restore(sender, playerName, id);
                })
                .withRequirement(sender ->
                        sender.hasPermission("buildmc.admin") ||
                                sender.hasPermission("buildmc.restore-deaths")
                );

        deathsCmd.withSubcommand(listCmd);
        deathsCmd.withSubcommand(restoreCmd);

        return deathsCmd;
    }
}
