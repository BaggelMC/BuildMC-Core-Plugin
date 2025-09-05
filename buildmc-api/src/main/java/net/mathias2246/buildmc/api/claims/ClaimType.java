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
     * Teams are represented using the vanilla scoreboard team system.
     * </p>
     */
    TEAM,

    /**
     * A claim owned by the server.
     * <p>
     * The {@code ownerId} is always set to "server". Functionally, this type
     * behaves like a normal claim: it has protections, and users with
     * {@code buildmc.admin} permission can modify it.
     * There is no limit to the claim amount.
     * This type is used to designate areas as being under server control.
     * </p>
     */
    SERVER,

    /**
     * A placeholder claim that does not provide any protections.
     * <p>
     * The {@code ownerId} is also set to "server". Unlike normal claims,
     * this type does not restrict player actions. Its sole purpose is to
     * reserve an area so that it cannot be claimed by players. This is useful
     * for preventing conflicts in specific regions without modifying their
     * existing properties or gameplay.
     * </p>
     */
    PLACEHOLDER
}
