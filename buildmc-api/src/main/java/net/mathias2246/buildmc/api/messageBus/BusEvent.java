package net.mathias2246.buildmc.api.messageBus;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public class BusEvent {
    private final String channelId;
    private final SemVer version;
    private final String publisherId;
    private final JsonObject payload;
    private final long timestamp;

    public BusEvent(@NotNull String channelId, @NotNull SemVer version, @NotNull String publisherId, @NotNull JsonObject payload, long timestamp) {
        this.channelId = channelId;
        this.version = version;
        this.publisherId = publisherId;
        this.payload = payload;
        this.timestamp = timestamp;
    }

    public @NotNull String getChannelId() {
        return channelId;
    }

    public @NotNull SemVer getVersion() {
        return version;
    }

    public @NotNull String getPublisherId() {
        return publisherId;
    }

    public @NotNull JsonObject getPayload() {
        return payload;
    }

    public long getTimestamp() {
        return timestamp;
    }
}