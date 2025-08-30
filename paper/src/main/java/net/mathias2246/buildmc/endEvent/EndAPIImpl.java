package net.mathias2246.buildmc.endEvent;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.api.endevent.EndAPI;
import net.mathias2246.buildmc.api.endevent.EndChangeCause;
import net.mathias2246.buildmc.api.endevent.EndState;
import net.mathias2246.buildmc.api.event.endevent.EndStateChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import static net.mathias2246.buildmc.Main.config;
import static net.mathias2246.buildmc.Main.configFile;

public class EndAPIImpl implements EndAPI {

    private static final String OPEN_ANNOUNCEMENT_KEY = "messages.end-event.broadcast-opened";
    private static final String CLOSE_ANNOUNCEMENT_KEY = "messages.end-event.broadcast-closed";

    @Override
    public boolean openEnd(@Nullable CommandSender sender, @NotNull EndChangeCause cause, @NotNull String announcementKey) {
        return changeEndState(EndState.OPEN, sender, cause, announcementKey);
    }

    @Override
    public boolean closeEnd(@Nullable CommandSender sender, @NotNull EndChangeCause cause, @NotNull String announcementKey) {
        return changeEndState(EndState.CLOSED, sender, cause, announcementKey);
    }

    @Override
    public boolean openEnd(@Nullable CommandSender sender, @NotNull EndChangeCause cause) {
        return openEnd(sender, cause, OPEN_ANNOUNCEMENT_KEY);
    }

    @Override
    public boolean closeEnd(@Nullable CommandSender sender, @NotNull EndChangeCause cause) {
        return closeEnd(sender, cause, CLOSE_ANNOUNCEMENT_KEY);
    }

    @Override
    public @NotNull EndState getCurrentState() {
        return EndListener.allowEnd ? EndState.OPEN : EndState.CLOSED;
    }

    /**
     * Handles both opening and closing the End.
     */
    private boolean changeEndState(@NotNull EndState targetState,
                                   @Nullable CommandSender sender,
                                   @NotNull EndChangeCause cause,
                                   @NotNull String announcementKey) {

        EndStateChangeEvent event = new EndStateChangeEvent(
                targetState,
                getCurrentState(),
                cause,
                sender,
                announcementKey
        );

        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return false;

        // Apply the new state
        boolean allowEnd = event.getNewState() != EndState.CLOSED;
        performChangeLogic(event.getAnnouncementKey(), allowEnd);

        return true;
    }

    /**
     * Updates internal state, saves config, and notifies all players.
     */
    private void performChangeLogic(String announcementKey, boolean allowEnd) {
        EndListener.allowEnd = allowEnd;
        config.set("end-event.allow-end", allowEnd);
        try {
            config.save(configFile);
        } catch (IOException ignored) {
        }

        Component msg = Component.translatable(announcementKey);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(msg);
        }
    }
}
