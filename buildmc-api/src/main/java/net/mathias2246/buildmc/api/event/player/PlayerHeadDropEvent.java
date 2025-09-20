package net.mathias2246.buildmc.api.event.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Called when a {@link Player}'s head is about to be dropped after death.
 * <p>
 * Plugins can listen to this event to:
 * <ul>
 *   <li>Cancel the head drop</li>
 *   <li>Modify the {@link ItemStack} representing the head</li>
 *   <li>Attach metadata for other plugins to consume</li>
 * </ul>
 * This event is {@link Cancellable}.
 */
public class PlayerHeadDropEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    /** Whether the event has been cancelled */
    private boolean cancelled = false;

    /** Optional metadata map for plugins to store additional info */
    private final Map<String, Object> metadata = new HashMap<>();

    private final Player victim;
    private final Player killer;
    private ItemStack playerHead;
    private final PlayerDeathEvent event;

    /**
     * Constructs a new {@code PlayerHeadDropEvent}.
     *
     * @param victim     the player who died (the owner of the head)
     * @param killer     the player who killed the victim, may be {@code null}
     * @param playerHead the {@link ItemStack} representing the victim's head
     */
    public PlayerHeadDropEvent(Player victim, Player killer, ItemStack playerHead, PlayerDeathEvent event) {
        this.victim = victim;
        this.killer = killer;
        this.playerHead = playerHead;
        this.event = event;
    }

    /**
     * Gets the victim whose head is being dropped.
     *
     * @return the victim player
     */
    public Player getVictim() {
        return victim;
    }

    /**
     * Gets the killer of the victim.
     *
     * @return the killer player, or {@code null} if the death was not caused by another player
     */
    public Player getKiller() {
        return killer;
    }

    /**
     * Gets the {@link ItemStack} representing the player's head.
     *
     * @return the head item
     */
    public ItemStack getPlayerHead() {
        return playerHead;
    }

    /**
     * Sets the {@link ItemStack} to drop as the player's head.
     *
     * @param playerHead the new head item
     */
    public void setPlayerHead(ItemStack playerHead) {
        this.playerHead = playerHead;
    }

    /**
     * Gets the {@link PlayerDeathEvent} that caused the head to drop.
     *
     * @return the head item
     */
    public PlayerDeathEvent getEvent() {
        return event;
    }

    /**
     * Gets an unmodifiable view of the metadata map.
     *
     * @return the metadata map
     */
    @NotNull
    public Map<String, Object> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    /**
     * Adds or updates a metadata entry.
     *
     * @param key   the metadata key
     * @param value the metadata value
     */
    public void putMetadata(@NotNull String key, @NotNull Object value) {
        metadata.put(key, value);
    }

    /**
     * Removes a metadata entry.
     *
     * @param key the metadata key to remove
     */
    public void removeMetadata(@NotNull String key) {
        metadata.remove(key);
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

    /**
     * Gets the list of handlers for this event instance.
     *
     * @return the handler list
     */
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
