package net.mathias2246.buildmc.api.event.endevent;

import net.mathias2246.buildmc.api.endEvent.EndChangeCause;
import net.mathias2246.buildmc.api.endEvent.EndState;
import net.mathias2246.buildmc.api.event.CustomEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when the End state is about to change.
 * <p>
 * This event is cancellable. Plugins can inspect or modify the changing process,
 * including the announcement key, the cause, and the sender. Listeners can also
 * attach arbitrary metadata to share information across other listeners.
 * </p>
 */
public class EndStateChangeEvent extends CustomEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    /** Whether the event has been cancelled */
    private boolean cancelled = false;

    /**
     * The state that the End is transitioning to.
     * This represents the new state after the event is processed.
     */
    private final EndState newState;

    /**
     * The previous state of the End before this change occurred.
     * May be null if there was no prior state or if the previous state is unknown.
     */
    private final EndState prevState;

    /** The cause of the End state changing (e.g., command, plugin, scheduled) */
    private final EndChangeCause cause;

    /** The sender responsible for changing the End state, if applicable */
    private final CommandSender sender;

    /** The translatable key used for the announcement message */
    private String announcementKey;

    /**
     * Constructs a new EndStateChangeEvent.
     *
     * @param newState        the new state the End is transitioning to; must not be null
     * @param previousState   the previous state of the End; may be null if there was no prior state
     * @param cause           the reason the End state is changing; defaults to {@link EndChangeCause#OTHER} if null
     * @param sender          the CommandSender responsible for the change, or null if not applicable
     * @param announcementKey the translatable key used for the announcement message
     */
    public EndStateChangeEvent(
            @NotNull EndState newState,
            @Nullable EndState previousState,
            @Nullable EndChangeCause cause,
            @Nullable CommandSender sender,
            @NotNull String announcementKey) {

        this.newState = newState;
        this.prevState = previousState;

        if (cause == null) cause = EndChangeCause.OTHER;

        this.cause = cause;
        this.sender = sender;
        this.announcementKey = announcementKey;
    }

    /**
     * Gets the state that the End is transitioning to.
     *
     * @return the new EndState; never null
     */
    @NotNull
    public EndState getNewState() {
        return newState;
    }

    /**
     * Gets the previous state of the End before this event.
     *
     * @return the previous EndState, or null if there was none
     */
    @Nullable
    public EndState getPreviousState() {
        return prevState;
    }

    /**
     * Gets the cause of the End state changing.
     *
     * @return the cause
     */
    @NotNull
    public EndChangeCause getCause() {
        return cause;
    }

    /**
     * Gets the sender responsible for changing the End state.
     *
     * @return the CommandSender, or null if not applicable
     */
    @Nullable
    public CommandSender getCommandSender() {
        return sender;
    }

    /**
     * Gets the translation key for the announcement message.
     *
     * @return the translation key
     */
    @NotNull
    public String getAnnouncementKey() {
        return announcementKey;
    }

    /**
     * Sets the translation key for the announcement message.
     *
     * @param announcementKey the new translation key
     */
    public void setAnnouncementKey(@NotNull String announcementKey) {
        this.announcementKey = announcementKey;
    }

    /**
     * Checks whether the event has been cancelled.
     *
     * @return true if cancelled, false otherwise
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets whether the event is cancelled.
     *
     * @param cancelled true to cancel the event, false to allow it
     */
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Gets the static list of handlers for this event type.
     * Required by Bukkit for event registration.
     *
     * @return the handler list
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
