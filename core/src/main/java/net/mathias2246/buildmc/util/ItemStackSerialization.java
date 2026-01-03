package net.mathias2246.buildmc.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class ItemStackSerialization {

    private static final Gson gson = new Gson();

    public static byte[] serialize(ItemStack item) throws IllegalArgumentException {
        Map<String, Object> serialized = item.serialize();
        String json = gson.toJson(serialized);
        return json.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    public static ItemStack deserialize(byte[] bytes) throws IllegalArgumentException {
        String json = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        Map<String, Object> map = gson.fromJson(json, new TypeToken<>() {});
        return ItemStack.deserialize(map);
    }
}
