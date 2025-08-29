package net.mathias2246.buildmc.api.endevent;

import net.mathias2246.buildmc.api.event.endevent.EndStateChangeEvent;

/**
 * Represents the reason why the End is being opened or closed.
 * <p>
 * Used in {@link EndStateChangeEvent}
 * and other End-related events to provide context to listeners.
 * </p>
 */
public enum EndChangeCause {

    /** The End is being opened or closed due to a player or admin command. */
    COMMAND,

    /** The End is being opened or closed programmatically by another plugin. */
    PLUGIN,

    /** The End is being opened or closed for any other reason not covered by COMMAND or PLUGIN. */
    OTHER
}
