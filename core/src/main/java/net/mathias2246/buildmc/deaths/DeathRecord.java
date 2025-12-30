package net.mathias2246.buildmc.deaths;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public record DeathRecord(long id, UUID playerUuid, long timestamp, int xp, String cause, Map<Integer, byte[]> items) {

    /**
     * Returns an unmodifiable view of the item map.
     * Keys = inventory slots, values = serialized ItemStack bytes.
     */
    @Override
    public Map<Integer, byte[]> items() {
        return Collections.unmodifiableMap(items);
    }
}
