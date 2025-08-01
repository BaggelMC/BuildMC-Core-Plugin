package net.mathias2246.buildmc.claims;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@SuppressWarnings("unused")
public class ClaimManager {

    public static final @NotNull NamespacedKey CLAIM_PCD_KEY = Objects.requireNonNull(NamespacedKey.fromString("buildmc:claim_owner"));

    /**Forcefully sets the owner of the chunk at the given location.
     * @param team The team that should own the chunk or null if the current owner should be removed.*/
    public static void forceClaimChunk(@Nullable Team team, @NotNull Location location) {
        if (team == null) {
            location.getChunk().getPersistentDataContainer().remove(CLAIM_PCD_KEY); // TODO: Test if contains check is needed
            return;
        }
        location.getChunk().getPersistentDataContainer().set(
                CLAIM_PCD_KEY, PersistentDataType.STRING, team.getName()
        );
    }

    /**Forcefully sets the owner of the chunk at the given location.
     * @param team The team that should own the chunk or null if the current owner should be removed.*/
    public static void forceClaimChunk(@Nullable Team team, @NotNull Chunk chunk) {
        if (team == null) {
            chunk.getPersistentDataContainer().remove(CLAIM_PCD_KEY); // TODO: Test if contains check is needed
            return;
        }
        chunk.getPersistentDataContainer().set(
                CLAIM_PCD_KEY, PersistentDataType.STRING, team.getName()
        );
    }

    // Gets the owner string directly form PDC or null if no owner was set
    private static @Nullable String getOwnerString(@NotNull Location location) {

        var chunk = location.getChunk();

        if (!chunk.getPersistentDataContainer().has(CLAIM_PCD_KEY)) return null;
        return chunk.getPersistentDataContainer().get(
                CLAIM_PCD_KEY, PersistentDataType.STRING
        );
    }

    // Gets the owner string directly form PDC or null if no owner was set
    private static @Nullable String getOwnerString(@NotNull Chunk chunk) {
        if (!chunk.getPersistentDataContainer().has(CLAIM_PCD_KEY)) return null;
        return chunk.getPersistentDataContainer().get(
                CLAIM_PCD_KEY, PersistentDataType.STRING
        );
    }

    /**Checks if the given chunk has an owner or not*/
    public static boolean hasOwner(@NotNull Chunk chunk) {
        return chunk.getPersistentDataContainer().has(CLAIM_PCD_KEY);
    }

    /**Checks if the chunk has an owner or if it is already owned by the given team.*/
    public static boolean isNotClaimedOrOwn(Team team, @NotNull Location location) {
        Team c = getClaimTeam(location);
        return Objects.equals(
                team,
                c
        ) || c == null;
    }

    /**Checks if the chunk has an owner or if it is already owned by the given team.*/
    public static boolean isNotClaimedOrOwn(Team team, @NotNull Chunk chunk) {
        Team c = getClaimTeam(chunk);
        return Objects.equals(
                team,
                c
        ) || c == null;
    }

    /** Gets the players team
     * @return the team the player is currently on, or null if he has no team*/
    public static @Nullable Team getPlayerTeam(@NotNull Player player) {
        return player.getScoreboard().getEntryTeam(player.getName());
    }

    /**Gets the team that claims the chunk at the given location.
     * @return The team that owns the given chunk or null if no one owns this chunk*/
    public static @Nullable Team getClaimTeam(@NotNull Location location) {
        var owner = getOwnerString(location);
        if (owner == null) return null;

        return Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard().getTeam(owner);
    }

    /**Gets the team that claims the chunk at the given location.
     * @return The team that owns the given chunk or null if no one owns this chunk*/
    public static @Nullable Team getClaimTeam(@NotNull Chunk chunk) {
        var owner = getOwnerString(chunk);
        if (owner == null) return null;

        return Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard().getTeam(owner);
    }

    public static boolean isPlayerAllowed(@NotNull Player player, @NotNull Location location) {

        return false;
    }


    public static void forceClaimArea(Team team, @NotNull Location from, @NotNull Location to) {
        if (!Objects.equals(from.getWorld(), to.getWorld())) {
            return;
        }
        for (int z = from.getChunk().getZ(); z <= to.getChunk().getZ(); z++) {
            for (int x = from.getChunk().getX(); x <= to.getChunk().getX(); x++) {
                Chunk chunk = Objects.requireNonNull(from.getWorld()).getChunkAt(x, z);
                forceClaimChunk(team, chunk);
            }
        }
    }

    public static void forceClaimArea(Team team, @NotNull World world, int startX, int startZ, int endX, int endZ) {
        for (int z = startZ; z <= endZ; z++) {
            for (int x = startZ; x <= endZ; x++) {
                Chunk chunk = world.getChunkAt(x, z);
                forceClaimChunk(team, chunk);
            }
        }
    }

    /**Gets the number of chunks the given team has left to claim*/
    public static int getChunksLeft(@NotNull Team team) {
        return Integer.MAX_VALUE;
    }
}
