package net.mathias2246.buildmc.api.team;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Team {

    private final @Nullable TeamManager teamManager;


    private final @NotNull HashSet<UUID> members;

    public Team(@NotNull TeamManager teamManager) {
        this.teamManager = teamManager;
        members = new HashSet<>();
    }

    public boolean isMember(@NotNull Entity entity) {
        return members.contains(entity.getUniqueId());
    }

    public boolean isMember(@NotNull UUID uuid) {
        return members.contains(uuid);
    }

    public void addMember(@NotNull Entity entity) {
        if (teamManager != null) {

        }
        members.add(entity.getUniqueId());

    }

    public void addMember(@NotNull UUID uuid) {

    }

    public void removeMember(@NotNull Entity entity) {

    }

    public void removeMember(@NotNull UUID uuid) {

    }
}
