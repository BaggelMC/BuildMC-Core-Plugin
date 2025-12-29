package net.mathias2246.buildmc.util;

import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class ItemStackSerialization {

    public static byte[] serialize(ItemStack item) throws IllegalArgumentException {
        Map<String, Object> serialized = item.serialize();
        String json = new com.google.gson.Gson().toJson(serialized);
        return json.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    public static ItemStack deserialize(byte[] bytes) throws IllegalArgumentException {
        String json = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        Map<String, Object> map = new com.google.gson.Gson().fromJson(json, Map.class);
        return ItemStack.deserialize(map);
    }
}
