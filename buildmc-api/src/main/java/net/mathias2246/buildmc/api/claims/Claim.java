package net.mathias2246.buildmc.api.claims;

import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.UUID;

@SuppressWarnings({"unused", "BooleanMethodIsAlwaysInverted"})
public class Claim {

    private Long id;

    private final ClaimType type;
    private final String ownerId;
    private final UUID worldId;
    private final int chunkX1, chunkZ1;
    private final int chunkX2, chunkZ2;
    private final String name;

    private final List<UUID> whitelistedPlayers;

    private final List<String> protections;

    public Claim(Long id, ClaimType type, String ownerId, UUID worldId,
                 int chunkX1, int chunkZ1, int chunkX2, int chunkZ2, String name,
                 List<UUID> whitelistedPlayers, List<String> protectionFlags) {

        this.id = id;
        this.type = type;
        this.ownerId = ownerId;
        this.worldId = worldId;
        this.chunkX1 = chunkX1;
        this.chunkZ1 = chunkZ1;
        this.chunkX2 = chunkX2;
        this.chunkZ2 = chunkZ2;
        this.name = name;

        this.whitelistedPlayers = whitelistedPlayers;

        if (protectionFlags.isEmpty()) {
            this.protections = Protection.defaultProtections.stream().toList();
        } else {
            this.protections = protectionFlags;
        }
    }

    /**Gets the id of this claim*/
    public Long getId() { return id; }
    public ClaimType getType() { return type; }
    public String getOwnerId() { return ownerId; }
    public UUID getWorldId() { return worldId; }
    public int getChunkX1() { return chunkX1; }
    public int getChunkZ1() { return chunkZ1; }
    public int getChunkX2() { return chunkX2; }
    public int getChunkZ2() { return chunkZ2; }
    public String getName() { return name; }

    /**This method sets the id of a claim instance.
     * <p>This method is only used internally. You should not use this method!</p>*/
    @ApiStatus.Internal
    public void setID(long id) { this.id = id; }

    /**@return A list containing all the UUIDS of players that are whitelisted inside this claim.*/
    public List<UUID> getWhitelistedPlayers() { return whitelistedPlayers; }

    /**Checks if a player is whitelisted on this claim, granting him permission to do anything on the claim.
     * @return True if, the given UUID is whitelisted.*/
    public boolean isPlayerWhitelisted(UUID playerID) { return whitelistedPlayers.contains(playerID); }

    /**Adds a player to this claims whitelist, granting him permission to do anything on the claim.*/
    public void addWhitelistedPlayer(UUID playerId) {
        if (!whitelistedPlayers.contains(playerId)) {
            whitelistedPlayers.add(playerId);
        }
    }

    /**Removes a player from this claims whitelist, taking away his permissions on this claim.*/
    public void removeWhitelistedPlayer(UUID playerId) {
        whitelistedPlayers.remove(playerId);
    }

    /**@return An EnumSet containing the protections that are enabled on this claim.*/
    public List<String> getProtections() { return protections; }

    /**@return True if, this claim has a certain ProtectionFlag set; otherwise false*/
    public boolean hasFlag(NamespacedKey protection) { return protections.contains(protection.toString()); }

    /**@return True if, this claim has a certain ProtectionFlag set; otherwise false*/
    public boolean hasFlag(Protection protection) { return protections.contains(protection.getKey().toString()); }

    /**Adds a protection flag to this claim.
     * @param protection The ProtectionFlag to add.*/
    public void addProtectionFlag(NamespacedKey protection) { protections.add(protection.toString()); }

    /**Removes a protection flag from this claim.
     * @param protection The ProtectionFlag to remove.*/
    public void removeProtectionFlag(NamespacedKey protection) { protections.remove(protection.toString()); }

    /**Checks if the given chunk is inside this claim.
     * @return True if, this claim is inside the same world and area.*/
    public boolean contains(int chunkX, int chunkZ, UUID worldUUID) {
        if (!this.worldId.equals(worldUUID)) return false;
        int minX = Math.min(chunkX1, chunkX2);
        int maxX = Math.max(chunkX1, chunkX2);
        int minZ = Math.min(chunkZ1, chunkZ2);
        int maxZ = Math.max(chunkZ1, chunkZ2);
        return chunkX >= minX && chunkX <= maxX && chunkZ >= minZ && chunkZ <= maxZ;
    }

    /**Checks if the given chunk is inside this claim.
     * @return True if, this claim is inside the same world and area.*/
    public boolean contains(Chunk chunk) {
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
