package net.mathias2246.buildmc.deaths;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public class DeathRecord {

    private final long id;
    private final UUID playerUuid;
    private final long timestamp;
    private final int xp;
    private final String cause;
    private final Map<Integer, byte[]> items;

    public DeathRecord(
            long id,
            UUID playerUuid,
            long timestamp,
            int xp,
            String cause,
            Map<Integer, byte[]> items
    ) {
        this.id = id;
        this.playerUuid = playerUuid;
        this.timestamp = timestamp;
        this.xp = xp;
        this.cause = cause;
        this.items = items;
    }

    public long getId() {
        return id;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getXp() {
        return xp;
    }

    public String getCause() {
        return cause;
    }

    /**
     * Returns an unmodifiable view of the item map.
     * Keys = inventory slots, values = serialized ItemStack bytes.
     */
    public Map<Integer, byte[]> getItems() {
        return Collections.unmodifiableMap(items);
    }
}
