package net.mathias2246.buildmc.api.endEvent;

/**
 * Represents the current state of the End dimension.
 * <p>
 * This enum is used to indicate whether the End is currently open or closed.
 * </p>
 */
public enum EndState {

    /**
     * The End is currently open and accessible to players.
     */
    OPEN,

    /**
     * The End is currently closed and cannot be entered by players.
     */
    CLOSED
}
