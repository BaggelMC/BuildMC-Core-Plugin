package net.mathias2246.buildmc.team;

import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.event.team.TeamMemberAddEvent;
import net.mathias2246.buildmc.api.event.team.TeamMemberRemovedEvent;
import net.mathias2246.buildmc.api.event.team.TeamRegisteredEvent;
import net.mathias2246.buildmc.api.event.team.TeamUnregisterEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TeamSync implements Listener {

    @EventHandler
    public void onUnregister(TeamUnregisterEvent event) {
        CoreMain.plugin.getLogger().info("Unregistering team " + event.getTeam().getName());
    }

    @EventHandler
    public void onRegister(TeamRegisteredEvent event) {
        CoreMain.plugin.getLogger().info("Registering team " + event.getTeam().getName());
    }

    @EventHandler
    public void onMemberAdd(TeamMemberAddEvent event) {
        CoreMain.plugin.getLogger().info("Adding team member to team " + event.getTeam().getName());
    }

    @EventHandler
    public void onMemberRemove(TeamMemberRemovedEvent event) {
        CoreMain.plugin.getLogger().info("Removing team member from team " + event.getTeam().getName());
    }
}
