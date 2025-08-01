package net.mathias2246.buildmc.claims;

import net.mathias2246.buildmc.util.ConfigurationManager;
import org.bukkit.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings({"unused", "BooleanMethodIsAlwaysInverted"})
public class ClaimManager extends ConfigurationManager{


    public static final @NotNull NamespacedKey CLAIM_PCD_KEY = Objects.requireNonNull(NamespacedKey.fromString("buildmc:claim_owner"));

    public @NotNull Map<Team, List<UUID>> claimWhitelists = new HashMap<>();

    public ClaimManager(@NotNull Plugin plugin, @NotNull String resourceName) {
        super(plugin, resourceName);
    }

    @Override
    public void setupConfiguration() {
        claimWhitelists = new HashMap<>();
        for (var key : configuration.getKeys(false)) {

            Team team = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard().getTeam(key);
            List<UUID> uuids = new ArrayList<>();
            for (String id : configuration.getStringList(key)) {
                uuids.add(UUID.fromString(id));
            }
            claimWhitelists.put(team, uuids);
        }
    }

    /**Checks if the given player is whitelisted in the claims of the given team.
     *
     * @return True, if the player is whitelisted, false if not whitelisted or the team doesn't exist
     * */
    public static boolean isPlayerWhitelisted(@NotNull ClaimManager manager, Team team, @NotNull Player player) {
        if (team == null) return false;
        var l = manager.claimWhitelists.get(team);
        if (l == null) return false;

        return l.contains(player.getUniqueId());
    }

    /**Checks if the given player is whitelisted in the claims of the given team.
     *
     * @return True, if the player is whitelisted, false if not whitelisted or the team doesn't exist
     * */
    public static boolean isPlayerWhitelisted(@NotNull ClaimManager manager, Team team, @NotNull HumanEntity player) {
        if (team == null) return false;
        var l = manager.claimWhitelists.get(team);
        if (l == null) return false;

        return l.contains(player.getUniqueId());
    }

    /**Adds a Player to the team whitelist*/
    public static void setPlayerWhitelisted(@NotNull ClaimManager manager, @NotNull Team team, @NotNull Player player) {
        if (!manager.claimWhitelists.containsKey(team)) manager.claimWhitelists.put(team, new ArrayList<>());
        var t = manager.claimWhitelists.get(team);
        t.add(player.getUniqueId());
    }

    /**Removes a player from the team whitelist*/
    public static void removePlayerWhitelisted(@NotNull ClaimManager manager, @NotNull Team team, @NotNull Player player) {
        if (!manager.claimWhitelists.containsKey(team)) manager.claimWhitelists.put(team, new ArrayList<>());
        var t = manager.claimWhitelists.get(team);
        t.remove(player.getUniqueId());
    }

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
    public static @Nullable String getOwnerString(@NotNull Location location) {

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

    /**Checks if the chunk has an owner or if it is already owned by the given team.*/
    public static boolean isNotClaimedOrOwn(@NotNull Player player, @NotNull Chunk chunk) {
        Team c = getClaimTeam(chunk);
        return Objects.equals(
                getPlayerTeam(player),
                c
        ) || c == null;
    }

    /**Checks if the chunk has an owner or if it is already owned by the given team.*/
    public static boolean isNotClaimedOrOwn(@NotNull HumanEntity player, @NotNull Chunk chunk) {
        Team c = getClaimTeam(chunk);
        return Objects.equals(
                getPlayerTeam((Player) player),
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

    public static boolean isPlayerAllowed(@NotNull ClaimManager manager, @NotNull Player player, @NotNull Location location) {
        return isNotClaimedOrOwn(player, player.getLocation().getChunk()) || isPlayerWhitelisted(manager, getClaimTeam(location), player);
    }

    public static boolean isPlayerAllowed(@NotNull ClaimManager manager, @NotNull HumanEntity player, @NotNull Location location) {
        return isNotClaimedOrOwn(player, player.getLocation().getChunk()) || isPlayerWhitelisted(manager, getClaimTeam(location), player);
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
