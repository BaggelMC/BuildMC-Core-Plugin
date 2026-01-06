package net.mathias2246.buildmc.api.claims;

import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a claimed area of land within a world.
 * <p>
 * A {@code Claim} is defined by its owner, claim type, world, and a rectangular
 * region of chunks. Claims may have additional attributes such
 * as a name, whitelisted players, and protections.
 * </p>
 */
@SuppressWarnings({"unused", "BooleanMethodIsAlwaysInverted"})
public class Claim {

    private Long id;

    private final ClaimType type;
    private String ownerId;
    private final UUID worldId;
    private final int chunkX1, chunkZ1;
    private final int chunkX2, chunkZ2;
    private String name;

    private final List<UUID> whitelistedPlayers;

    private final List<String> protections;

    /**
     * Constructs a new claim.
     *
     * @param id                 The database ID of this claim, or {@code null} if not yet persisted.
     * @param type               The {@link ClaimType} of this claim.
     * @param ownerId            The ID of the owner (usually a player UUID as string, a team name or "server" depending on the {@link ClaimType}).
     * @param worldId            The UUID of the world where this claim is located.
     * @param chunkX1            The X-coordinate of the first chunk corner.
     * @param chunkZ1            The Z-coordinate of the first chunk corner.
     * @param chunkX2            The X-coordinate of the opposite chunk corner.
     * @param chunkZ2            The Z-coordinate of the opposite chunk corner.
     * @param name               The display name of this claim.
     * @param whitelistedPlayers The list of players who are whitelisted in this claim.
     * @param protections        The list of protections (as string keys).
     *                           If empty and the Claim is not a placeholder, the default protections will be applied.
     */
    public Claim(@Nullable Long id, @NotNull ClaimType type, @NotNull String ownerId, @NotNull UUID worldId,
                 int chunkX1, int chunkZ1, int chunkX2, int chunkZ2, @NotNull String name,
                 @NotNull List<UUID> whitelistedPlayers, @NotNull List<String> protections) {

        this.id = id;
        this.type = type;
        this.ownerId = ownerId;
        this.worldId = worldId;
        this.chunkX1 = chunkX1;
        this.chunkZ1 = chunkZ1;
        this.chunkX2 = chunkX2;
        this.chunkZ2 = chunkZ2;
        this.name = name;

        this.whitelistedPlayers = new ArrayList<>(whitelistedPlayers); // Ensure it's mutable

        if (protections.isEmpty() && type != ClaimType.PLACEHOLDER) {
            this.protections = new ArrayList<>(Protection.defaultProtections);
        } else {
            this.protections = new ArrayList<>(protections);
        }
    }

    /**
     * Gets the database ID of this claim.
     *
     * @return The ID, or {@code null} if not yet persisted.
     */
    @Nullable public Long getId() { return id; }

    /**
     * Gets the type of this claim.
     *
     * @return The claim type.
     */
    public ClaimType getType() { return type; }

    /**
     * Gets the owner ID of this claim.
     *
     * @return The owner ID (usually a player UUID as string, a team name or "server" depending on the {@link ClaimType}).
     */
    public String getOwnerId() { return ownerId; }

    /**
     * Sets the owner ID of this claim.
     * <p>
     * This method is intended for internal use only. Use ClaimManager to set the Owner ID. (usually a player UUID as string, a team name or "server" depending on the {@link ClaimType})
     * </p>
     *
     * @param ownerId The new owner ID.
     */
    @ApiStatus.Internal
    public void setOwnerId(@NotNull String ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * Gets the world UUID where this claim exists.
     *
     * @return The world UUID.
     */
    public UUID getWorldId() { return worldId; }

    /**
     * Gets the first X-coordinate of the claimed area.
     *
     * @return The X-coordinate of the first corner chunk.
     */
    public int getChunkX1() { return chunkX1; }

    /**
     * Gets the first Z-coordinate of the claimed area.
     *
     * @return The Z-coordinate of the first corner chunk.
     */
    public int getChunkZ1() { return chunkZ1; }

    /**
     * Gets the second X-coordinate of the claimed area.
     *
     * @return The X-coordinate of the opposite corner chunk.
     */
    public int getChunkX2() { return chunkX2; }

    /**
     * Gets the second Z-coordinate of the claimed area.
     *
     * @return The Z-coordinate of the opposite corner chunk.
     */
    public int getChunkZ2() { return chunkZ2; }

    /**
     * Gets the display name of this claim.
     *
     * @return The name of the claim
     */
    public String getName() { return name; }

    /**
     * Sets the claim name of this claim.
     * <p>
     * This method is intended for internal use only. Use ClaimManager to change claim name.
     * </p>
     *
     * @param name The new claim name.
     */
    @ApiStatus.Internal
    public void setName(@NotNull String name) {
        this.name = name;
    }

    /**
     * Sets the database ID of this claim.
     * <p>
     * This method is intended for internal use only. DO NOT OVERRIDE THE ID OF CLAIMS THAT DON'T BELONG TO YOU.
     * </p>
     *
     * @param id The new claim ID.
     */
    @ApiStatus.Internal
    public void setID(Long id) { this.id = id; }

    /**
     * Gets the list of whitelisted players in this claim.
     *
     * @return A list of whitelisted player UUIDs.
     */
    public List<UUID> getWhitelistedPlayers() { return whitelistedPlayers; }

    /**
     * Checks if a player is whitelisted in this claim.
     *
     * @param playerID The player's UUID.
     * @return {@code true} if the player is whitelisted, otherwise {@code false}.
     */
    public boolean isPlayerWhitelisted(@NotNull UUID playerID) { return whitelistedPlayers.contains(playerID); }

    /**
     * Adds a player to the whitelist of this claim.
     * <p>
     * This method is intended for internal use only. Use ClaimManager to add a player to the whitelist.
     *
     * @param playerId The UUID of the player to add.
     */
    @ApiStatus.Internal
    public void addWhitelistedPlayer(@NotNull UUID playerId) {
        if (!whitelistedPlayers.contains(playerId)) {
            whitelistedPlayers.add(playerId);
        }
    }

    /**
     * Removes a player from the whitelist of this claim.
     * <p>
     * This method is intended for internal use only. Use ClaimManager to remove a player from the whitelist.
     *
     * @param playerId The UUID of the player to remove.
     */
    @ApiStatus.Internal
    public void removeWhitelistedPlayer(@NotNull UUID playerId) {
        whitelistedPlayers.remove(playerId);
    }

    /**
     * Gets the list of protections applied to this claim.
     *
     * @return A list of protections keys as strings.
     */
    public List<String> getProtections() { return protections; }

    /**
     * Checks if this claim has a specific protection.
     *
     * @param protection The {@link NamespacedKey} of the protection.
     * @return {@code true} if the protection is present, otherwise {@code false}.
     */
    public boolean hasProtection(@NotNull NamespacedKey protection) { return protections.contains(protection.toString()); }

    /**
     * Checks if this claim has a specific protection.
     *
     * @param protection The {@link Protection} object representing the protection.
     * @return {@code true} if the protection is present, otherwise {@code false}.
     */
    public boolean hasProtection(@NotNull Protection protection) { return protections.contains(protection.getKey().toString()); }

    /**
     * Adds a protection to this claim.
     *
     * <p>
     * This method is intended for internal use only. Use ClaimManager to add protections.
     *
     * @param protection The {@link NamespacedKey} of the protection to add.
     */
    @ApiStatus.Internal
    public void addProtection(@NotNull NamespacedKey protection) { protections.add(protection.toString()); }

    /**
     * Removes a protection from this claim.
     *
     * <p>
     * This method is intended for internal use only. Use ClaimManager to remove protections.
     *
     * @param protection The {@link NamespacedKey} of the protection to remove.
     */
    @ApiStatus.Internal
    public void removeProtection(@NotNull NamespacedKey protection) { protections.remove(protection.toString()); }

    /**
     * Checks whether this claim contains the given chunk coordinates in the given world.
     *
     * @param chunkX    The X-coordinate of the chunk.
     * @param chunkZ    The Z-coordinate of the chunk.
     * @param worldUUID The UUID of the world.
     * @return {@code true} if the chunk is inside this claim, otherwise {@code false}.
     */
    public boolean contains(int chunkX, int chunkZ, UUID worldUUID) {
        if (!this.worldId.equals(worldUUID)) return false;
        int minX = Math.min(chunkX1, chunkX2);
        int maxX = Math.max(chunkX1, chunkX2);
        int minZ = Math.min(chunkZ1, chunkZ2);
        int maxZ = Math.max(chunkZ1, chunkZ2);
        return chunkX >= minX && chunkX <= maxX && chunkZ >= minZ && chunkZ <= maxZ;
    }

    /**
     * Checks whether this claim contains the given chunk.
     *
     * @param chunk The {@link Chunk} to check.
     * @return {@code true} if the chunk is inside this claim, otherwise {@code false}.
     */
    public boolean contains(@NotNull Chunk chunk) {
        return contains(chunk.getX(), chunk.getZ(), chunk.getWorld().getUID());
    }

    @Override
    public String toString() {
        return "Claim{" +
                "id=" + id +
                "type=" + type +
                ", ownerId='" + ownerId + '\'' +
                ", worldId=" + worldId +
                ", chunkX1=" + chunkX1 +
                ", chunkZ1=" + chunkZ1 +
                ", chunkX2=" + chunkX2 +
                ", chunkZ2=" + chunkZ2 +
                ", name='" + name + '\'' +
                ", whitelistedPlayers=" + whitelistedPlayers +
                ", protections=" + protections +
                '}';
    }
}
