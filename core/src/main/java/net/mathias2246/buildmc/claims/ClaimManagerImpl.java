package net.mathias2246.buildmc.claims;

import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.claims.ClaimManager;
import net.mathias2246.buildmc.api.claims.Protection;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class ClaimManagerImpl implements ClaimManager {
    @Override
    public @Nullable Team getPlayerTeam(@NotNull Player player) {
        return net.mathias2246.buildmc.claims.ClaimManager.getPlayerTeam(player);
    }

    @Override
    public boolean isPlayerAllowed(@NotNull Player player, @NotNull Collection<NamespacedKey> protections, Location location) {
        return net.mathias2246.buildmc.claims.ClaimManager.isPlayerAllowed(player, protections, location);
    }

    @Override
    public boolean isPlayerAllowed(@NotNull Player player, @NotNull Collection<NamespacedKey> protections, @Nullable Claim claim) {
        return net.mathias2246.buildmc.claims.ClaimManager.isPlayerAllowed(player, protections, claim);
    }

    @Override
    public boolean isPlayerAllowed(@NotNull Player player, @NotNull NamespacedKey protection, @Nullable Claim claim) {
        return net.mathias2246.buildmc.claims.ClaimManager.isPlayerAllowed(player, protection, claim);
    }

    @Override
    public boolean isPlayerAllowed(@NotNull Player player, @NotNull NamespacedKey protection, Location location) {
        return net.mathias2246.buildmc.claims.ClaimManager.isPlayerAllowed(player, protection, location);
    }

    @Override
    public boolean hasAnyProtection(Claim claim, Collection<NamespacedKey> protections) {
        return net.mathias2246.buildmc.claims.ClaimManager.hasAnyProtection(claim, protections);
    }

    @Override
    public boolean hasProtection(Claim claim, NamespacedKey protection) {
        return net.mathias2246.buildmc.claims.ClaimManager.hasProtection(claim, protection);
    }

    @Override
    public boolean hasAllProtections(Claim claim, Collection<String> flags) {
        return net.mathias2246.buildmc.claims.ClaimManager.hasAllProtections(claim, flags);
    }

    @Override
    public boolean hasAllProtectionKeys(Claim claim, Collection<NamespacedKey> keys) {
        return net.mathias2246.buildmc.claims.ClaimManager.hasAllProtectionKeys(claim, keys);
    }

    @Override
    public boolean isClaimInArea(UUID worldID, int chunkX1, int chunkZ1, int chunkX2, int chunkZ2) throws SQLException {
        return net.mathias2246.buildmc.claims.ClaimManager.isClaimInArea(worldID, chunkX1, chunkZ1, chunkX2, chunkZ2);
    }

    @Override
    public List<Claim> getClaimsInArea(Location pos1, Location pos2) throws SQLException, IllegalArgumentException {
        return net.mathias2246.buildmc.claims.ClaimManager.getClaimsInArea(pos1, pos2);
    }

    @Override
    public boolean isClaimed(Chunk chunk) {
        return net.mathias2246.buildmc.claims.ClaimManager.isClaimed(chunk);
    }

    @Override
    public @Nullable Long getClaimId(Chunk chunk) {
        return net.mathias2246.buildmc.claims.ClaimManager.getClaimId(chunk);
    }

    @Override
    public @Nullable Claim getClaim(Chunk chunk) throws SQLException {
        return net.mathias2246.buildmc.claims.ClaimManager.getClaim(chunk);
    }

    @Override
    public @Nullable Claim getClaimByID(long claimID) {
        return net.mathias2246.buildmc.claims.ClaimManager.getClaimByID(claimID);
    }

    @Override
    public @Nullable Claim getClaim(Location location) throws SQLException {
        return net.mathias2246.buildmc.claims.ClaimManager.getClaim(location);
    }

    @Override
    public boolean tryClaimPlayerArea(@NotNull Player player, String claimName, Location pos1, Location pos2) throws IllegalArgumentException {
        if (pos1 == null || pos2 == null) {
            throw new IllegalArgumentException("Positions cannot be null.");
        }

        if (pos1.getWorld() == null || pos2.getWorld() == null) {
            throw new IllegalArgumentException("Both locations must have a world.");
        }

        if (!pos1.getWorld().equals(pos2.getWorld())) {
            throw new IllegalArgumentException("Locations must be in the same world.");
        }

        return net.mathias2246.buildmc.claims.ClaimManager.tryClaimPlayerArea(player, claimName, pos1, pos2);
    }

    @Override
    public boolean tryClaimTeamArea(@NotNull Team team, String claimName, Location pos1, Location pos2) throws IllegalArgumentException {
        if (pos1 == null || pos2 == null) {
            throw new IllegalArgumentException("Positions cannot be null.");
        }

        if (pos1.getWorld() == null || pos2.getWorld() == null) {
            throw new IllegalArgumentException("Both locations must have a world.");
        }

        if (!pos1.getWorld().equals(pos2.getWorld())) {
            throw new IllegalArgumentException("Locations must be in the same world.");
        }

        return net.mathias2246.buildmc.claims.ClaimManager.tryClaimTeamArea(team, claimName, pos1, pos2);
    }

    @Override
    public void addPlayerToWhitelist(long claimID, UUID playerID) {
        net.mathias2246.buildmc.claims.ClaimManager.addPlayerToWhitelist(claimID, playerID);
    }

    @Override
    public void removePlayerFromWhitelist(long claimID, UUID playerID) {
        net.mathias2246.buildmc.claims.ClaimManager.removePlayerFromWhitelist(claimID, playerID);
    }

    @Override
    public void addProtection(long claimId, @NotNull Protection protection) {
        net.mathias2246.buildmc.claims.ClaimManager.addProtection(claimId, protection);
    }

    @Override
    public void addProtection(long claimId, @NotNull NamespacedKey protection) {
        net.mathias2246.buildmc.claims.ClaimManager.addProtection(claimId, protection);
    }

    @Override
    public void addProtection(@NotNull Claim claim, @NotNull Protection protection) {
        net.mathias2246.buildmc.claims.ClaimManager.addProtection(claim, protection);
    }

    @Override
    public void addProtection(@NotNull Claim claim, @NotNull NamespacedKey protection) {
        net.mathias2246.buildmc.claims.ClaimManager.addProtection(claim, protection);
    }

    @Override
    public void removeProtection(long claimId, @NotNull Protection protection) {
        net.mathias2246.buildmc.claims.ClaimManager.removeProtection(claimId, protection);
    }

    @Override
    public void removeProtection(long claimId, @NotNull NamespacedKey protection) {
        net.mathias2246.buildmc.claims.ClaimManager.removeProtection(claimId, protection);
    }

    @Override
    public void removeProtection(@NotNull Claim claim, @NotNull Protection protection) {
        net.mathias2246.buildmc.claims.ClaimManager.removeProtection(claim, protection);
    }

    @Override
    public void removeProtection(@NotNull Claim claim, @NotNull NamespacedKey protection) {
        net.mathias2246.buildmc.claims.ClaimManager.removeProtection(claim, protection);
    }

    @Override
    public @Nullable String getClaimNameById(long claimId) {
        return net.mathias2246.buildmc.claims.ClaimManager.getClaimNameById(claimId);
    }

    @Override
    public boolean removeClaimById(long claimId) {
        return net.mathias2246.buildmc.claims.ClaimManager.removeClaimById(claimId);
    }

    @Override
    public boolean doesOwnerHaveClaimWithName(String ownerId, String claimName) throws SQLException {
        return net.mathias2246.buildmc.claims.ClaimManager.doesOwnerHaveClaimWithName(ownerId, claimName);
    }
}
