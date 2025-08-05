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

    /**The namespaced key used to store the owner inside the chunks PersistentDataContainer*/
    public static final @NotNull NamespacedKey CLAIM_PCD_KEY = Objects.requireNonNull(NamespacedKey.fromString("buildmc:claim_owner"));

    /**A Map containing all the claim data*/
    public Map<String, ClaimDataInstance> claims;

    public @NotNull ClaimDataInstance getEntryOrNew(@NotNull Team team) {

        claims.putIfAbsent(team.getName(), new ClaimDataInstance(null));
        return claims.get(team.getName());
    }

    public ClaimManager(@NotNull Plugin plugin, @NotNull String resourceName) {
        super(plugin, resourceName);
    }

    @Override
    public void setupConfiguration() {
        claims = new HashMap<>();
        for (var key : configuration.getKeys(false)) {
            var sect = configuration.getConfigurationSection(key);
            if (sect == null) return;

            Team team = Objects.requireNonNull(this.getPlugin().getServer().getScoreboardManager()).getMainScoreboard().getTeam(key);
            if (team == null) continue;

            var v = ClaimDataInstance.deserialize(sect.getValues(false));

            claims.putIfAbsent(team.getName(), v);
        }
    }

    @Override
    protected void preSave() {
        for (var entry : claims.entrySet()) {
            configuration.set(
                    entry.getKey(),
                    entry.getValue().serialize()
            );
        }
    }

    /**Checks if the given player is whitelisted in the claims of the given team.
     *
     * @return True, if the player is whitelisted, false if not whitelisted or the team doesn't exist
     * */
    public static boolean isPlayerWhitelisted(@NotNull ClaimManager manager, Team team, @NotNull Player player) {
        if (team == null) return false;
        var l = manager.getEntryOrNew(team);

        return l.whitelistedPlayers.contains(player.getUniqueId());
    }

    /**Checks if the given player is whitelisted in the claims of the given team.
     *
     * @return True, if the player is whitelisted, false if not whitelisted or the team doesn't exist
     * */
    public static boolean isPlayerWhitelisted(@NotNull ClaimManager manager, Team team, @NotNull HumanEntity player) {
        if (team == null) return false;
        var l = manager.getEntryOrNew(team);

        return l.whitelistedPlayers.contains(player.getUniqueId());
    }

    /**Adds a Player to the team whitelist*/
    public static void setPlayerWhitelisted(@NotNull ClaimManager manager, @NotNull Team team, @NotNull Player player) {
        var t = manager.getEntryOrNew(team).whitelistedPlayers;

        UUID uuid = player.getUniqueId();

        t.add(uuid);

        String tn = team.getName();
    }

    /**Removes a player from the team whitelist*/
    public static void removePlayerWhitelisted(@NotNull ClaimManager manager, @NotNull Team team, @NotNull Player player) {
        var t = manager.getEntryOrNew(team);

        t.whitelistedPlayers.remove(player.getUniqueId());
    }

    /**Forcefully sets the owner of the chunk at the given location.
     * @param team The team that should own the chunk or null if the current owner should be removed.*/
    public static void forceClaimChunk(@Nullable Team team, @NotNull Location location) {
        if (team == null) {
            location.getChunk().getPersistentDataContainer().remove(CLAIM_PCD_KEY);

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
            chunk.getPersistentDataContainer().remove(CLAIM_PCD_KEY);
            return;
        }

        chunk.getPersistentDataContainer().set(
                CLAIM_PCD_KEY, PersistentDataType.STRING, team.getName()
        );
    }

    /**Gets the owner string directly form the Persistent-Data-Container or null if no owner was set*/
    public static @Nullable String getOwnerString(@NotNull Location location) {
        return getOwnerString(location.getChunk());
    }

    // Gets the owner string directly form PDC or null if no owner was set
    public static @Nullable String getOwnerString(@NotNull Chunk chunk) {
        if (!chunk.getPersistentDataContainer().has(CLAIM_PCD_KEY, PersistentDataType.STRING)) return null;
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

    /**Checks if a player is allowed to do things at a certain location.
     * @return True if, the chunk at the location is not owned or is his own claim, or if he is whitelisted.*/
    public static boolean isPlayerAllowed(@NotNull ClaimManager manager, @NotNull Player player, @NotNull Location location) {
        return
                isNotClaimedOrOwn(player, location.getChunk()) ||
                isPlayerWhitelisted(manager, getClaimTeam(location), player) ||
                player.hasPermission("buildmc.bypass-claims");

    }

    /**Checks if a player is allowed to do things at a certain location.
     * @return True if, the chunk at the location is not owned or is his own claim, or if he is whitelisted.*/
    public static boolean isPlayerAllowed(@NotNull ClaimManager manager, @NotNull HumanEntity player, @NotNull Location location) {
        return
                isNotClaimedOrOwn(player, location.getChunk()) ||
                isPlayerWhitelisted(manager, getClaimTeam(location), player) ||
                player.hasPermission("buildmc.bypass-claims");
    }

    /**Forcefully claims an entire area for a team.
     * The area is set to the given team or all claims are removed if the parameter team is set to null.
     * @param from must be in the same world as the other location
     * @param to must be in the same world as the other location*/
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

    /**Forcefully claims an entire area for a team.
     * The area is set to the given team or all claims are removed if the parameter team is set to null.*/
    public static void forceClaimArea(Team team, @NotNull World world, int startX, int startZ, int endX, int endZ) {
        for (int z = startZ; z <= endZ; z++) {
            for (int x = startX; x <= endX; x++) {
                Chunk chunk = world.getChunkAt(x, z);
                forceClaimChunk(team, chunk);

            }
        }
    }

    /**Gets the number of chunks the given team has left to claim
     * If the amount of chunks left is smaller than zero, the claim limit will be disabled.*/
    public static int getChunksLeft(@NotNull ClaimManager manager, @NotNull Team team) {
        var t = manager.getEntryOrNew(team);
        return t.chunksLeft;
    }

}
