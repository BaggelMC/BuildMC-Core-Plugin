package net.mathias2246.buildmc.api.spawneyltra;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * API for interacting with BuildMC's spawn-elytra system.
 * <p>
 * Spawn-elytra allows players to glide and boost within a defined region
 * even if they do not have an actual elytra equipped.
 * </p>
 */
public interface ElytraAPI {

    /**
     * Checks whether the spawn-elytra feature is currently enabled
     * according to BuildMC's configuration.
     *
     * @return true if spawn-elytra is enabled, false otherwise
     */
    boolean isElytraEnabled();

    /**
     * Checks if the given player is currently inside a spawn-elytra zone.
     *
     * @param player the player to check
     * @return true if the player is inside a spawn-elytra zone
     */
    boolean isInElytraZone(@NotNull Player player);

    /**
     * Checks if the given location is inside a spawn-elytra zone.
     *
     * @param location the location to check
     * @return true if the location is inside a spawn-elytra zone
     */
    boolean isInElytraZone(@NotNull Location location);

    /**
     * Registers or replaces the active spawn-elytra zone.
     * The zone is defined as the cuboid between two corner positions.
     *
     * @param pos1  the first corner of the zone
     * @param pos2  the opposite corner of the zone
     * @param world the world the zone belongs to
     */
    void registerZone(@NotNull Location pos1, @NotNull Location pos2, @NotNull World world);

    /**
     * Grants or revokes spawn-elytra flight for a player.
     *
     * @param player  the player to modify
     * @param flying  true to enable flight, false to disable
     */
    void setPlayerFlying(@NotNull Player player, boolean flying);

    /**
     * Checks if the given player is currently flying with spawn-elytra.
     *
     * @param player the player to check
     * @return true if the player is using spawn-elytra flight
     */
    boolean isUsingSpawnElytra(@NotNull Player player);

    /**
     * Checks if the given entity is currently flying with spawn-elytra.
     *
     * @param entity the entity to check
     * @return true if the entity is using spawn-elytra flight
     */
    boolean isUsingSpawnElytra(@NotNull Entity entity);

    /**
     * Checks whether the given player has already used a boost
     * during their current flight session.
     *
     * @param player the player to check
     * @return true if the player has already boosted
     */
    boolean hasBoosted(@NotNull Player player);

    /**
     * Checks whether the given entity has already used a boost
     * during their current flight session.
     *
     * @param entity the entity to check
     * @return true if the entity has already boosted
     */
    boolean hasBoosted(@NotNull Entity entity);

    /**
     * Resets the "boost used" flag for the given player,
     * allowing them to boost again within the same flight.
     * <p>
     * Does not remove the current boost effect, only resets the metadata.
     * </p>
     *
     * @param player the player whose boost state should be reset
     */
    void resetBoost(@NotNull Player player);

    /**
     * Resets the "boost used" flag for the given entity,
     * allowing them to boost again within the same flight.
     * <p>
     * Does not remove the current boost effect, only resets the metadata.
     * </p>
     *
     * @param entity the entity whose boost state should be reset
     */
    void resetBoost(@NotNull Entity entity);

    /**
     * Applies a spawn-elytra boost to the given player and marks them
     * as having boosted for this flight session.
     *
     * @param player           the player to boost
     * @param multiplier       the velocity multiplier
     * @param verticalVelocity the vertical velocity to apply
     */
    void applyBoost(@NotNull Player player, int multiplier, double verticalVelocity);

    /**
     * Applies a spawn-elytra boost to the given player using default
     * multiplier and vertical velocity values, and marks them as boosted.
     *
     * @param player the player to boost
     */
    void applyBoost(@NotNull Player player);

    /**
     * Applies a spawn-elytra boost to the given player without marking them
     * as boosted, allowing multiple boosts per flight.
     *
     * @param player           the player to boost
     * @param multiplier       the velocity multiplier
     * @param verticalVelocity the vertical velocity to apply
     */
    void applyRawBoost(@NotNull Player player, int multiplier, double verticalVelocity);

    /**
     * Applies a spawn-elytra boost to the given player using default
     * values, without marking them as boosted.
     *
     * @param player the player to boost
     */
    void applyRawBoost(@NotNull Player player);
}
