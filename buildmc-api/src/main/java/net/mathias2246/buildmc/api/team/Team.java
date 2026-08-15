package net.mathias2246.buildmc.api.team;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.mathias2246.buildmc.api.event.team.TeamMemberAddEvent;
import net.mathias2246.buildmc.api.event.team.TeamMemberRemovedEvent;
import net.mathias2246.buildmc.util.MetadataHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public class Team extends MetadataHolder {

    private @Nullable TeamManager teamManager;

    private final @NotNull HashSet<UUID> members;

    public @Nullable TextColor getColor() {
        return color;
    }

    public void setColor(@Nullable TextColor color) {
        this.color = color;
    }

    public @Nullable Component getDisplayName() {
        return displayName;
    }

    public void setDisplayName(@Nullable Component displayName) {
        this.displayName = displayName;
    }

    private @Nullable TextColor color = null;
    private @Nullable Component displayName = null;

    public @NotNull String getName() {
        return name;
    }

    private final @NotNull String name;

    public Team(@NotNull String name) {
        this.teamManager = null;
        this.name = name;
        members = new HashSet<>();
    }

    public void register(@NotNull TeamManager manager, boolean persist) {
        this.teamManager = manager;
        if (manager.isRegisteredTeam(name)) return;

        manager.registerTeam(this, persist);
    }

    public void unregister() {
        if (teamManager == null) return;
        if (!teamManager.isRegisteredTeam(name)) return;

        var temp = teamManager;
        teamManager = null;
        temp.unregisterTeam(this);
    }

    @Contract(pure = true)
    public @NotNull Stream<UUID> members() {
        return Set.copyOf(members).stream();
    }

    /** Returns the {@link TeamManager} this team is registered in.
     *
     * @return The {@link TeamManager} this team is registered in, or {@code null} if not registered.
     * **/
    public @Nullable TeamManager getTeamManager() {
        return teamManager;
    }

    @Contract(value = "_ -> _", pure = true)
    public boolean isMember(@NotNull Entity entity) {
        return members.contains(entity.getUniqueId());
    }

    @Contract(value = "_ -> _", pure = true)
    public boolean isMember(@NotNull UUID uuid) {
        return members.contains(uuid);
    }

    /** Adds a {@link Entity} as a member to this team.
     *
     * @return {@code true} if the entities UUID wasn't already present in this team.
     * **/
    public boolean addMember(@NotNull Entity entity) {
        return addMember(entity.getUniqueId());
    }

    /** Adds a member by his {@link UUID} to this team.
     * Fires a cancellable {@link TeamMemberAddEvent}.
     * If {@code ignoreCancelled} is set to {@code false} and the event is cancelled, the member will not be added to the team.
     *
     * @param uuid The {@link UUID} of the member to try to add
     * @param cancellable If {@code true}, this action can be cancelled by event handlers.
     *
     * @return {@code true} if the {@link UUID} if the member wasn't added.
     *
     * @see Team#addMember(UUID)
     * **/
    public boolean addMember(@NotNull UUID uuid, boolean cancellable, boolean fireEvent) {
        if (teamManager != null) {
            var mTeam = teamManager.getTeamFromMember(uuid);
            if (mTeam != null && !mTeam.equals(this)) {
                mTeam.removeMember(uuid);
            }

            if (!members.contains(uuid)) {

                if (fireEvent) {
                    var event = new TeamMemberAddEvent(uuid, this, cancellable);
                    Bukkit.getPluginManager().callEvent(event);
                    if (cancellable && event.isCancelled()) return false;
                }
            }

            members.add(uuid);
            return teamManager.joinTeam(uuid, this);
        }

        return members.add(uuid);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Team t) {
            return t.name.equals(this.name);
        }
        return false;
    }

    /** Adds a member by his {@link UUID} to this team.
     * Fires a cancellable {@link TeamMemberAddEvent}.
     * If the event is cancelled, the member will not be added to the team.
     *
     * @param uuid The {@link UUID} of the member to try to add
     *
     * @return {@code true} if the {@link UUID} if the member wasn't added.
     *
     * @see Team#addMember(UUID, boolean, boolean) Ignore Cancelled
     * **/
    public boolean addMember(@NotNull UUID uuid) {
        return addMember(uuid, false, true);
    }

    /** Removes an {@link Entity} from this team.
     *
     * @return {@code true} if the entities UUID was present and removed successfully.
     *
     * @see Team#removeMember(UUID)
     * **/
    public boolean removeMember(@NotNull Entity entity) {
        return removeMember(entity.getUniqueId());
    }

    /** Removes a member from this team by his {@link UUID}.
     *
     * @return {@code true} if the {@link UUID} was present and removed successfully.
     *
     * @see Team#removeMember(Entity)
     * **/
    public boolean removeMember(@NotNull UUID uuid) {
        var result = members.remove(uuid);
        if (teamManager != null) {
            teamManager.leaveTeam(uuid, this);
            if (result) Bukkit.getPluginManager().callEvent(new TeamMemberRemovedEvent(uuid, this));
        }

        return result;
    }
}
