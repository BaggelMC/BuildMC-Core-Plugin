package net.mathias2246.buildmc.api.team;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.pointer.Pointered;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.mathias2246.buildmc.api.BuildMcAPI;
import net.mathias2246.buildmc.api.event.team.TeamMemberAddEvent;
import net.mathias2246.buildmc.api.event.team.TeamMemberRemovedEvent;
import net.mathias2246.buildmc.api.event.team.TeamPropertyChangedEvent;
import net.mathias2246.buildmc.util.MetadataHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** A custom team implementation.
 * <p>
 *     This team implementation is required on Folia servers, because there the vanilla team system is disabled.
 * </p>
 * <p>
 *     When creating a team instance, you can manually register it in a {@link TeamManager}.<br>
 *     E.g. via {@link BuildMcAPI#getTeamManager()}.<br>
 *     When adding members to an unregistered team, nothing else (e.g. the server or the added members)
 *     is informed that the team exists.<br>
 *     Once a team is registered, an uncancellable {@link TeamMemberAddEvent} is fired retroactively for each of its
 *     current members.
 * </p>
 * <p>
 *     A team is a collection of members.
 *     A member is represented as a {@link UUID}, not a {@link org.bukkit.entity.Player} or
 *     {@link org.bukkit.entity.Entity} directly - the UUID does not need to correspond to an existing entity.
 * </p>
 * <p>
 *     Like Bukkit's own {@link org.bukkit.scoreboard.Team}, this team is also an Adventure
 *     {@link Audience} and {@link ForwardingAudience}.
 * </p>
 *
 * @see org.bukkit.scoreboard.Team Bukkit Teams
 * */
public class Team extends MetadataHolder
        implements Iterable<UUID>, Pointered, Audience, ForwardingAudience, Cloneable {
    private @Nullable TeamManager teamManager;

    private final @NotNull HashSet<UUID> members;

    public @Nullable TextColor getColor() {
        return color;
    }

    public void setColor(@Nullable TextColor color) {
        if (teamManager != null) {
            Bukkit.getPluginManager().callEvent(new TeamPropertyChangedEvent(this));
        }
        this.color = color;
    }

    public @Nullable Component getDisplayName() {
        return displayName;
    }

    public void setDisplayName(@Nullable Component displayName) {
        if (teamManager != null) {
            Bukkit.getPluginManager().callEvent(new TeamPropertyChangedEvent(this));
        }
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

    /** Gets a {@link Stream} of all members on the team. */
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
     * (Optionally) Fires a cancellable {@link TeamMemberAddEvent}.
     * If {@code cancelled} is set to {@code true} and the event is cancelled, the member will not be added to the team.
     *
     * @param uuid The {@link UUID} of the member to try to add
     * @param cancellable If {@code true}, this action can be cancelled by event handlers.
     * @param fireEvent If a {@link TeamMemberAddEvent} should be fired
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

    /** Checks if there are any members on this team.
     *
     * @return {@code true} if there is no member on this team.
     *
     * @see Team#empty()
     * **/
    public boolean isEmpty() {
        return members.isEmpty();
    }

    /** Removes all members from this team.
     * This will fire a {@link TeamMemberRemovedEvent} for each member.
     *
     * @see Team#removeMember(UUID)
     * @see Team#removeMember(Entity)
     * **/
    public void empty() {
        for (UUID uuid : members) {
            removeMember(uuid);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Team t) {
            return t.hashCode() == this.hashCode();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /** Adds a member by his {@link UUID} to this team.
     * Fires an uncancellable {@link TeamMemberAddEvent}.
     *
     * @param uuid The {@link UUID} of the member to try to add
     *
     * @return {@code true} if the {@link UUID} if the member wasn't added.
     *
     * @see Team#addMember(UUID, boolean, boolean)
     * **/
    public boolean addMember(@NotNull UUID uuid) {
        return addMember(uuid, false, true);
    }

    /** Removes an {@link Entity} from this team.
     * This will fire a {@link TeamMemberRemovedEvent} when the member was successfully removed.
     *
     * @return {@code true} if the entities UUID was present and removed successfully.
     *
     * @see Team#removeMember(UUID)
     * **/
    public boolean removeMember(@NotNull Entity entity) {
        return removeMember(entity.getUniqueId());
    }

    /** Removes a member from this team by his {@link UUID}.
     * This will fire a {@link TeamMemberRemovedEvent} when the member was successfully removed.
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

    /** Used to iterate over all members on this team. **/
    @Override
    public @NonNull Iterator<UUID> iterator() {
        return members.iterator();
    }

    /** Iterates over all members on this team, and apply an action on each member.
     *
     * @param action The action to apply on each member {@link UUID}
     * **/
    @Override
    public void forEach(Consumer<? super UUID> action) {
        members.forEach(action);
    }

    @Override
    public Spliterator<UUID> spliterator() {
        return members().spliterator();
    }

    @Override
    public @NonNull Iterable<? extends Audience> audiences() {
        return members.stream()
                .map(Bukkit::getPlayer)      // null if offline
                .filter(Objects::nonNull)
                .map(Audience.class::cast)   // Player already implements Audience on Paper
                .collect(Collectors.toSet());
    }

    /** Creates a clone of this team.
     * <p>
     *     A new clone will not be automatically registered inside a {@link TeamManager}.
     * </p>
     * */
    @Override
    public Team clone() {
        try {
            Team clone = (Team) super.clone();
            clone.members.addAll(members);
            clone.teamManager = null;

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
