package net.mathias2246.buildmc.commands.debug;

import net.mathias2246.buildmc.CoreMain;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TeamManagerDebuging {

    public static void giveDebugBook(@NotNull Player player) {
        player.getInventory().addItem(CoreMain.teamManager.buildDebugBook());
    }

    public static void getAssignedTeam(@NotNull CommandSender sender, @NotNull Player player) {
        var team = CoreMain.teamManager.getTeamFromMember(player);
        if (team == null) {
            sender.sendMessage("No team assigned...");
            return;
        }
        sender.sendMessage("Player " + player.getName() + " has been assigned to " + team.getName());
    }

}
