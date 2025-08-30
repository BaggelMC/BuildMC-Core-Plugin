package net.mathias2246.buildmc.api.endevent;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * API for interacting with BuildMC's End event system.
 * <p>
 * Provides methods to open or close the End, either with a custom
 * announcement key or using the plugin's default keys.
 */
public interface EndAPI {

    /**
     * Requests that the End be opened with a custom announcement key.
     *
     * @param sender          the CommandSender triggering the open; may be null
     * @param cause           the reason the End is opening
     * @param announcementKey the announcement message key
     * @return true if the operation was successful (not cancelled)
     */
    boolean openEnd(@Nullable CommandSender sender,
                    @NotNull EndChangeCause cause,
                    @NotNull String announcementKey);

    /**
     * Requests that the End be closed with a custom announcement key.
     *
     * @param sender          the CommandSender triggering the close; may be null
     * @param cause           the reason the End is closing
     * @param announcementKey the announcement message key
     * @return true if the operation was successful (not cancelled)
     */
    boolean closeEnd(@Nullable CommandSender sender,
                     @NotNull EndChangeCause cause,
                     @NotNull String announcementKey);

    /**
     * Requests that the End be opened using BuildMC's default announcement key.
     *
     * @param sender the CommandSender triggering the open; may be null
     * @param cause  the reason the End is opening
     * @return true if the operation was successful (not cancelled)
     */
    boolean openEnd(@Nullable CommandSender sender,
                    @NotNull EndChangeCause cause);

    /**
     * Requests that the End be closed using BuildMC's default announcement key.
     *
     * @param sender the CommandSender triggering the close; may be null
     * @param cause  the reason the End is closing
     * @return true if the operation was successful (not cancelled)
     */
    boolean closeEnd(@Nullable CommandSender sender,
                     @NotNull EndChangeCause cause);

    /**
     * Returns the current state of the End.
     *
     * @return the current {@link EndState}, never null
     */
    @NotNull
    EndState getCurrentState();
}
