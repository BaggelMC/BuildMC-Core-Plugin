package net.mathias2246.buildmc.team;

import net.kyori.adventure.text.format.NamedTextColor;
import net.mathias2246.buildmc.api.event.team.*;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

public class TeamSync implements Listener {

    public final @NotNull Scoreboard scoreboard;

    public TeamSync() {
        scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    }

    @EventHandler
    public void onUnregister(TeamUnregisterEvent event) {
        var team = scoreboard.getTeam(event.getTeam().getName());
        if (team == null) return;
        team.unregister();
    }

    @EventHandler
    public void onRegister(TeamRegisteredEvent event) {
        scoreboard.registerNewTeam(event.getTeam().getName());
    }

    @EventHandler
    public void onMemberAdd(TeamMemberAddEvent event) {
        var  team = scoreboard.getTeam(event.getTeam().getName());
        if (team == null) return;
        team.addEntry(event.getMemberId().toString());
    }

    @EventHandler
    public void onMemberRemove(TeamMemberRemovedEvent event) {
        var  team = scoreboard.getTeam(event.getTeam().getName());
        if (team == null) return;
        team.removeEntry(event.getMemberId().toString());
    }

    @EventHandler
    public void onPropertyChanged(TeamPropertyChangedEvent event) {
        var team = scoreboard.getTeam(event.getTeam().getName());
        if (team == null) return;
        team.color(event.getTeam().getColor() == null ? null : NamedTextColor.nearestTo(event.getTeam().getColor()));
        team.displayName(event.getTeam().getDisplayName());
    }
}
