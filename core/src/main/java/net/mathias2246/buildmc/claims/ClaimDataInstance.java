package net.mathias2246.buildmc.claims;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public class ClaimDataInstance implements ConfigurationSerializable {

    public final @NotNull List<UUID> whitelistedPlayers = new ArrayList<>();
    public int chunksLeft;


    public static int defaultChunksLeftAmount = -1;

    public ClaimDataInstance() {

    }

    public ClaimDataInstance(@Nullable Integer chunksLeft) {
        this.chunksLeft = chunksLeft == null ? defaultChunksLeftAmount : chunksLeft;
    }

    // Deserialize constructor
    public ClaimDataInstance(List<String> uuidStrings, int chunksLeft) {
        for (String uuid : uuidStrings) {
            whitelistedPlayers.add(UUID.fromString(uuid));
        }
        this.chunksLeft = chunksLeft;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        List<String> uuidStrings = new ArrayList<>();
        for (UUID uuid : whitelistedPlayers) {
            uuidStrings.add(uuid.toString());
        }
        map.put("whitelist", uuidStrings);
        map.put("chunks-left", chunksLeft);

        return map;
    }


    @SuppressWarnings("unchecked")
    public static ClaimDataInstance deserialize(Map<String, Object> map) {
        List<String> whitelist = (List<String>) map.getOrDefault("whitelist", new ArrayList<>());
        int chunks = (Integer) map.getOrDefault("chunks-left", defaultChunksLeftAmount);

        return new ClaimDataInstance(whitelist, chunks);
    }
}