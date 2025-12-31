package net.mathias2246.buildmc.deaths;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mathias2246.buildmc.CoreMain;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static net.mathias2246.buildmc.CoreMain.plugin;

public class DeathsCommand {

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static int list(CommandSender sender, String playerName) {
        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            plugin.sendMessage(sender, Component.translatable("messages.deaths.error.player-not-found"));
            return 0;
        }

        UUID uuid = target.getUniqueId();

        try (Connection conn = CoreMain.databaseManager.getConnection()) {
            var deaths = CoreMain.deathTable.getDeathsByPlayer(conn, uuid);

            if (deaths.isEmpty()) {
                CoreMain.plugin.sendMessage(sender, Component.translatable("messages.deaths.none", Component.text(playerName)));
                return 1;
            }

            CoreMain.plugin.sendMessage(sender, Component.text("------------------------------", NamedTextColor.GRAY));
            CoreMain.plugin.sendMessage(sender, Component.translatable("messages.deaths.list-part").color(NamedTextColor.GOLD)
                    .append(Component.text(playerName).color(NamedTextColor.YELLOW))
                    .append(Component.text(":", NamedTextColor.GOLD)));


            for (var death : deaths) {
                Component line = Component.text("[ID ")
                        .color(NamedTextColor.GREEN)
                        .append(Component.text(death.id()).color(NamedTextColor.GREEN))
                        .append(Component.text("] "))
                        .append(Component.text(DATE_FORMAT.format(new Date(death.timestamp())), NamedTextColor.WHITE))
                        .append(Component.text(" - ").color(NamedTextColor.DARK_GRAY))
                        .append(Component.text(death.cause(), NamedTextColor.GRAY));

                CoreMain.plugin.sendMessage(sender, line);
            }

             CoreMain.plugin.sendMessage(sender, Component.text("------------------------------", NamedTextColor.GRAY));

            return 1;
        } catch (Exception e) {
            CoreMain.plugin.getLogger().warning(e.toString());
            CoreMain.plugin.sendMessage(sender, Component.translatable("messages.deaths.error.load-failed"));
            return 0;
        }
    }

    public static int restore(CommandSender sender, String playerName, long deathId) {
        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
             CoreMain.plugin.sendMessage(sender, Component.translatable("messages.deaths.error.player-not-found"));
            return 0;
        }

        try (Connection conn = CoreMain.databaseManager.getConnection()) {

            DeathRecord record = CoreMain.deathTable.getDeathById(conn, deathId);
            if (record == null) {
                 CoreMain.plugin.sendMessage(sender, Component.translatable("messages.deaths.error.not-found"));
                return 0;
            }

            if (!record.playerUuid().equals(target.getUniqueId())) {
                 CoreMain.plugin.sendMessage(sender, Component.translatable("messages.deaths.error.wrong-player"));
                return 0;
            }

            DeathRestoreUtil.restore(target, record);
            plugin.sendMessage(sender, Component.translatable("messages.deaths.restored"));

            return 1;

        } catch (Exception e) {
            CoreMain.plugin.getLogger().warning(e.toString());
            CoreMain.plugin.sendMessage(sender, Component.translatable("messages.deaths.error.restore-failed"));
            return 0;
        }
    }
}
