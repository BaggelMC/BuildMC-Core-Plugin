package net.mathias2246.buildmc.api.messageBus;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record BusEvent(String channelId, SemVer version, String publisherId, JsonObject payload, long timestamp) {
    public BusEvent(@NotNull String channelId, @NotNull SemVer version, @NotNull String publisherId, @NotNull JsonObject payload, long timestamp) {
        this.channelId = channelId;
        this.version = version;
        this.publisherId = publisherId;
        this.payload = payload;
        this.timestamp = timestamp;
    }

    @Contract(pure = true)
    @Override
    public @NotNull String channelId() {
        return channelId;
    }

    @Contract(pure = true)
    @Override
    public @NotNull SemVer version() {
        return version;
    }

    @Contract(pure = true)
    @Override
    public @NotNull String publisherId() {
        return publisherId;
    }

    @Override
    public @NotNull JsonObject payload() {
        return payload;
    }
}