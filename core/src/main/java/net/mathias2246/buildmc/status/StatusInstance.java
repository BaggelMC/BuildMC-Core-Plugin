package net.mathias2246.buildmc.status;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.mathias2246.buildmc.claims.ClaimManager;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**The instance of a status that shows up inside the '/status set ...' command when registered.*/
@SerializableAs("StatusInstance")
public class StatusInstance implements ConfigurationSerializable {

    private final @NotNull String statusId;
    private final @Nullable Set<Permission> permissions;
    private final @Nullable Set<Team> teams;
    private final @NotNull Component display;
    private final boolean hasRequirements; // Only for caching

    /**@return The Text-Component that is displayed as a prefix before the players name.*/
    public @NotNull Component getDisplay() {
        return display;
    }

    /**@return The id of this status. <p>This is also displayed as the tab completion inside the '/status set ...' command.</p>*/
    public @NotNull String getStatusId() {
        return statusId;
    }

    /**Gets an optional set of Permissions, of which at least one is required to set this status.
     * @return The list of permissions, or null if not required.*/
    public @Nullable Set<Permission> getPermissions() {
        return permissions;
    }

    /**Gets an optional set of Teams, of which at least one is required to set this status.
     *  @return The list of teams, or null if not required.*/
    public @Nullable Set<Team> getTeams() {
        return teams;
    }

    public StatusInstance(@NotNull String statusId, @Nullable Set<Permission> permissions, @Nullable Set<Team> teams, @Nullable Component display) {
        this.statusId = statusId;
        this.permissions = permissions;
        this.teams = teams;
        hasRequirements = (permissions != null || teams != null);
        if (display == null) this.display = Component.text("null");
        else this.display = display;
    }

    /**Checks if the given player can have this status.
     * @return True if, the player has the optional permissions and or team and is not null.*/
    public AllowStatus allowPlayer(Player player) {
        if (player == null) return AllowStatus.NOT_ALLOWED;
        if (!hasRequirements) return AllowStatus.ALLOW;

        boolean allow = false;
        if (permissions != null) {
            for (var p : permissions) {
                if (player.hasPermission(p)) {
                    allow = true;
                    break;
                }
            }
        } else allow = true;
        if (!allow) return AllowStatus.MISSING_PERMISSION;

        if (teams != null) {
            Team team = ClaimManager.getPlayerTeam(player);
            if (team == null) return AllowStatus.NOT_IN_TEAM;

            for (var t : teams) {
                if (team.equals(t)) {
                    return AllowStatus.ALLOW;
                }
            }
            return AllowStatus.NOT_IN_TEAM;
        }
        return AllowStatus.ALLOW;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("statusId", statusId);
        if (permissions != null) {
            map.put("permissions", permissions.stream().map(Permission::getName).collect(Collectors.toList()));
        }
        if (teams != null) {
            map.put("teams", teams.stream().map(Team::getName).collect(Collectors.toList()));
        }
        map.put("display-name", MiniMessage.miniMessage().serialize(display));
        return map;
    }

    public static StatusInstance deserialize(Map<String, Object> map) {
        String statusId = (String) map.get("statusId");

        // Permissions as names -> convert back to Permission objects
        Set<Permission> perms = null;
        if (map.containsKey("permissions")) {
            if (map.get("permissions") instanceof List<?> permNames) {
                perms = permNames.stream().map((s) -> new Permission((String) s)).collect(Collectors.toSet());
            }
        }

        // Teams as names -> get from Bukkit Scoreboard
        Set<Team> teams = null;
        if (map.containsKey("teams")) {
            if ((map.get("teams") instanceof List<?> teamNames)) {
                teams = teamNames.stream()
                        .map(name -> Objects.requireNonNull(
                                Objects.requireNonNull(org.bukkit.Bukkit.getScoreboardManager())
                                        .getMainScoreboard().getTeam((String) name)))
                        .collect(Collectors.toSet());
            }
        }

        // display-name as MiniMessage Component
        Component display = null;
        if (map.containsKey("display-name")) {
            if (map.get("display-name") instanceof String s) {
                display = MiniMessage.miniMessage().deserialize(s);
            }
        }

        return new StatusInstance(statusId, perms, teams, display);
    }

    public enum AllowStatus {
        ALLOW,
        NOT_ALLOWED,
        MISSING_PERMISSION,
        NOT_IN_TEAM
    }
}
