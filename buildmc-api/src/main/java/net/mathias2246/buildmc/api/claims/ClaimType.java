package net.mathias2246.buildmc.api.claims;

/**
 * Represents the type of owner associated with a {@link Claim}.
 * <p>
 * Claims can be owned by different entities such as individual players,
 * teams, or the server itself. This enum is used to differentiate between
 * these ownership types.
 * </p>
 */
public enum ClaimType {

    /**
     * A claim owned by an individual player.
     * <p>
     * The {@code ownerId} of the claim should correspond to the player's
     * unique identifier (typically their UUID in string form).
     * </p>
     */
    PLAYER,

    /**
     * A claim owned by a team.
     * <p>
     * The {@code ownerId} of the claim should correspond to a team name.
     * Teams are the vanilla scoreboard team system.
     * </p>
     */
    TEAM,

    /**
     * Unused
     */
    SERVER,

    /**
     * Unused
     */
    PLACEHOLDER
}
