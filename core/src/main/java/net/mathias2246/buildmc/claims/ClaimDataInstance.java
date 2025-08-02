package net.mathias2246.buildmc.claims;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;


public class ClaimDataInstance implements ConfigurationSerializable {

    public @NotNull List<UUID> whitelistedPlayers = new ArrayList<>();
    public int chunksLeft = -1;


    public ClaimDataInstance() {
    }

    // Deserialize constructor
    public ClaimDataInstance(List<String> uuidStrings, int chunksLeft) {
        this();
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
        int chunks = (int) map.getOrDefault("chunks-left", -1);

        return new ClaimDataInstance(whitelist, chunks);
    }
}