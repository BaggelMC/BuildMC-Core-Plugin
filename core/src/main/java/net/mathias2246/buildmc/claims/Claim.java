package net.mathias2246.buildmc.claims;

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
            this.protections = protectionFlags;
            // TODO: Implement logic for default Protections
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

//    private String toConfigPath(ProtectionFlag flag) {
//        return switch (flag) {
//            case PLAYER_BREAK -> "claims.protections.player-break";
//            case PLAYER_PLACE -> "claims.protections.player-place";
//            case PLAYER_PLACE_ENTITY -> "claims.protections.player-place-entity";
//            case CONTAINER -> "claims.protections.containers";
//            case ITEM_PICKUP -> "claims.protections.item-pickup";
//            case ITEM_DROP -> "claims.protections.item-drop";
//            case SIGN_EDITING -> "claims.protections.sign-editing";
//            case SPLASH_POTIONS -> "claims.protections.splash-potions";
//            case VEHICLE_ENTER -> "claims.protections.vehicle-enter";
//            case BUCKET_USAGE -> "claims.protections.bucket-usage";
//            case FROST_WALKER -> "claims.protections.frostwalker";
//            case SCULK_SENSOR -> "claims.protections.sculk-sensor";
//            case PISTON_MOVEMENT_ACROSS_CLAIM_BORDERS -> "claims.protections.piston-movement-across-claim-borders";
//            case INTERACTION_JUKEBOX -> "claims.protections.interactions.jukebox";
//            case INTERACTION_LEVERS -> "claims.protections.interactions.levers";
//            case INTERACTION_BUTTONS -> "claims.protections.interactions.buttons";
//            case INTERACTION_REPEATERS -> "claims.protections.interactions.repeaters";
//            case INTERACTION_COMPARATORS -> "claims.protections.interactions.comparators";
//            case INTERACTION_DAYLIGHT_SENSORS -> "claims.protections.interactions.daylight-sensors";
//            case INTERACTION_PRESSURE_PLATES -> "claims.protections.interactions.pressure-plates";
//            case INTERACTION_TRIPWIRE -> "claims.protections.interactions.tripwire";
//            case INTERACTION_TRAPDOORS -> "claims.protections.interactions.trapdoors";
//            case INTERACTION_DOORS -> "claims.protections.interactions.doors";
//            case INTERACTION_FENCE_GATES -> "claims.protections.interactions.fence-gates";
//            case INTERACTION_FARMLAND -> "claims.protections.interactions.farmland";
//            case INTERACTION_CAMPFIRE -> "claims.protections.interactions.campfire";
//            case INTERACTION_BELLS -> "claims.protections.interactions.bells";
//            case INTERACTION_ATTACH_LEASH -> "claims.protections.interactions.attach-leash";
//            case INTERACTION_BONEMEAL -> "claims.protections.interactions.bonemeal";
//            case INTERACTION_BEEHIVES -> "claims.protections.interactions.beehives";
//            case INTERACTION_NAME_TAGS -> "claims.protections.interactions.nametags";
//            case INTERACTION_CANDLES -> "claims.protections.interactions.candle-extinguish";
//            case INTERACTION_HANGING_ENTITIES -> "claims.protections.interactions.hanging-entities";
//            case INTERACTION_LIGHT_TNT -> "claims.protections.interactions.light-tnt";
//            case INTERACTION_ARMOR_STAND -> "claims.protections.interactions.armor-stand";
//            case INTERACTION_TAME_ENTITY -> "claims.protections.interactions.tame-entity";
//            case INTERACTION_SHEAR_ENTITY -> "claims.protections.interactions.shear-entity";
//            case ENTITY_DAMAGE -> "claims.protections.damage.entity-damage";
//            case EXCLUDE_PLAYERS -> "claims.protections.damage.exclude-players";
//            case PROJECTILE_INTERACTIONS -> "claims.protections.projectile-interactions";
//            case FISHING -> "claims.protections.fishing";
//            case EXPLOSION_BLOCK_DAMAGE -> "claims.protections.damage.explosion-block-damage";
//            case EXPLOSION_ENTITY_DAMAGE -> "claims.protections.damage.explosion-entity-damage";
//            case ENTITY_MODIFICATIONS_WITHER -> "claims.protections.entity-modifications.wither";
//            case ENTITY_MODIFICATIONS_ENDERMAN -> "claims.protections.entity-modifications.enderman";
//            case ENTITY_MODIFICATIONS_RAVAGER -> "claims.protections.entity-modifications.ravager";
//        };
//    }

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
