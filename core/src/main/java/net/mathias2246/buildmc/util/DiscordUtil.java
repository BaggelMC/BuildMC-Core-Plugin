package net.mathias2246.buildmc.util;

import com.google.gson.JsonObject;
import net.mathias2246.buildmc.CoreMain;

import java.time.Instant;

public class DiscordUtil {

    public static JsonObject buildEmbed(String title, String description, int color, String footerText, String route) {
        JsonObject embed = new JsonObject();
        embed.addProperty("title", title);
        embed.addProperty("description", description);
        embed.addProperty("color", color);
        embed.addProperty("timestamp", Instant.now().toString());

        JsonObject footer = new JsonObject();
        footer.addProperty("text", footerText);
        embed.add("footer", footer);

        JsonObject payload = new JsonObject();
        payload.addProperty("route", route);
        payload.add("embed", embed);

        return payload;
    }

    public static void sendDiscordWebhook(JsonObject payload) {
        CoreMain.eventBus.publish(CoreMain.discordExtensionChannelKey, payload);
    }
}
