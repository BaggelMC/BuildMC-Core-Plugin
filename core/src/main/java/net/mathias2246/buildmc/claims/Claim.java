package net.mathias2246.buildmc.claims;

import net.mathias2246.buildmc.CoreMain;
import org.bukkit.Chunk;

import java.util.EnumSet;
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

    private final EnumSet<ProtectionFlag> protectionFlags;

    public Claim(Long id, ClaimType type, String ownerId, UUID worldId,
                 int chunkX1, int chunkZ1, int chunkX2, int chunkZ2, String name,
                 List<UUID> whitelistedPlayers, EnumSet<ProtectionFlag> protectionFlags) {

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
            this.protectionFlags = protectionFlags;
            for (ProtectionFlag flag : ProtectionFlag.values()) {
                String path = toConfigPath(flag);
                if (CoreMain.plugin.getConfig().getBoolean(path, false)) {
                    this.protectionFlags.add(flag);
                }
            }
        } else {
            this.protectionFlags = protectionFlags;
        }
    }

    public Long getId() { return id; }
    public ClaimType getType() { return type; }
    public String getOwnerId() { return ownerId; }
    public UUID getWorldId() { return worldId; }
    public int getChunkX1() { return chunkX1; }
    public int getChunkZ1() { return chunkZ1; }
    public int getChunkX2() { return chunkX2; }
    public int getChunkZ2() { return chunkZ2; }
    public String getName() { return name; }

    public void setID(long id) { this.id = id; }

    public List<UUID> getWhitelistedPlayers() { return whitelistedPlayers; }

    public boolean isPlayerWhitelisted(UUID playerID) { return whitelistedPlayers.contains(playerID); }

    public void addWhitelistedPlayer(UUID playerId) {
        if (!whitelistedPlayers.contains(playerId)) {
            whitelistedPlayers.add(playerId);
        }
    }

    public void removeWhitelistedPlayer(UUID playerId) {
        whitelistedPlayers.remove(playerId);
    }

    public EnumSet<ProtectionFlag> getProtectionFlags() { return protectionFlags; }

    public boolean hasFlag(ProtectionFlag flag) { return protectionFlags.contains(flag); }

    public boolean contains(int chunkX, int chunkZ, UUID worldUUID) {
        if (!this.worldId.equals(worldUUID)) return false;
        int minX = Math.min(chunkX1, chunkX2);
        int maxX = Math.max(chunkX1, chunkX2);
        int minZ = Math.min(chunkZ1, chunkZ2);
        int maxZ = Math.max(chunkZ1, chunkZ2);
        return chunkX >= minX && chunkX <= maxX && chunkZ >= minZ && chunkZ <= maxZ;
    }

    public boolean contains(Chunk chunk) {
        return contains(chunk.getX(), chunk.getZ(), chunk.getWorld().getUID());
    }

    private String toConfigPath(ProtectionFlag flag) {
        return switch (flag) {
            case PLAYER_BREAK -> "claims.protections.player-break";
            case PLAYER_PLACE -> "claims.protections.player-place";
            case CONTAINER -> "claims.protections.containers";
            case ALLOW_ENDER_CHESTS -> "claims.protections.allow-ender-chests";
            case ITEM_PICKUP -> "claims.protections.item-pickup";
            case ITEM_DROP -> "claims.protections.item-drop";
            case SIGN_EDITING -> "claims.protections.sign-editing";
            case SPLASH_POTIONS -> "claims.protections.splash-potions";
            case VEHICLE_ENTER -> "claims.protections.vehicle-enter";
            case BUCKET_USAGE -> "claims.protections.bucket-usage";
            case FROST_WALKER -> "claims.protections.frostwalker";
            case PISTON_MOVEMENT_ACROSS_CLAIM_BORDERS -> "claims.protections.piston-movement-across-claim-borders";
            case PREVENT_INTERACTIONS -> "claims.protections.prevent-interactions";
            case INTERACTION_LEVERS -> "claims.protections.interactions.levers";
            case INTERACTION_BUTTONS -> "claims.protections.interactions.buttons";
            case INTERACTION_REPEATERS -> "claims.protections.interactions.repeaters";
            case INTERACTION_COMPARATORS -> "claims.protections.interactions.comparators";
            case INTERACTION_PRESSURE_PLATES -> "claims.protections.interactions.pressure-plates";
            case INTERACTION_TRAPDOORS -> "claims.protections.interactions.trapdoors";
            case INTERACTION_DOORS -> "claims.protections.interactions.doors";
            case INTERACTION_FENCE_GATES -> "claims.protections.interactions.fence-gates";
            case INTERACTION_FARMLAND -> "claims.protections.interactions.farmland";
            case ENTITY_DAMAGE -> "claims.protections.damage.entity-damage";
            case EXCLUDE_PLAYERS -> "claims.protections.damage.exclude-players";
            case EXPLOSION_BLOCK_DAMAGE -> "claims.protections.damage.explosion-block-damage";
            case EXPLOSION_ENTITY_DAMAGE -> "claims.protections.damage.explosion-entity-damage";
            case PREVENT_ENTITY_MODIFICATIONS -> "claims.protections.prevent-entity-modifications";
            case ENTITY_MODIFICATIONS_WITHER -> "claims.protections.entity-modifications.wither";
            case ENTITY_MODIFICATIONS_ENDERMAN -> "claims.protections.entity-modifications.enderman";
            case ENTITY_MODIFICATIONS_RAVAGER -> "claims.protections.entity-modifications.ravager";
        };
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
                ", protectionFlags=" + protectionFlags +
                '}';
    }
}
