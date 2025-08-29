package net.mathias2246.buildmc.api.endevent;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface EndAPI {

    /**
     * Requests that the End be opened.
     *
     * @param sender          the CommandSender triggering the open; may be null if automated
     * @param cause           the reason the End is opening
     * @param announcementKey the announcement message key
     * @return true if the operation was successful (not cancelled)
     */
    boolean openEnd(@Nullable CommandSender sender, @NotNull EndChangeCause cause, @NotNull String announcementKey);

    /**
     * Requests that the End be closed.
     *
     * @param sender          the CommandSender triggering the close; may be null
     * @param cause           the reason the End is closing
     * @param announcementKey the announcement message key
     * @return true if the operation was successful (not cancelled)
     */
    boolean closeEnd(@Nullable CommandSender sender, @NotNull EndChangeCause cause, @NotNull String announcementKey);

    /**
     * Returns the current state of the End.
     */
    @NotNull
    EndState getCurrentState();
}
