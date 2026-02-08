package net.mathias2246.buildmc.api.event.player;

import net.mathias2246.buildmc.api.event.CustomPlayerEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

/**An event that is called when a player joins for the first time.
 * **/
public class PlayerFirstTimeJoinEvent extends CustomPlayerEvent {

    private static final @NotNull HandlerList HANDLER_LIST = new HandlerList();

    /**Gets the {@link PlayerJoinEvent} that triggered this event.**/
    public @NotNull PlayerJoinEvent getJoinEvent() {
        return joinEvent;
    }

    private final @NotNull PlayerJoinEvent joinEvent;

    /**Constructs a new {@link PlayerFirstTimeJoinEvent} from a {@link Player} and {@link PlayerJoinEvent}
     *
     * @param player The {@link Player} that joined for the first time
     * @param joinEvent The {@link PlayerJoinEvent} that triggered this event
     * **/
    public PlayerFirstTimeJoinEvent(@NotNull Player player, @NotNull PlayerJoinEvent joinEvent) {
        super(player);
        this.joinEvent = joinEvent;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static @NotNull HandlerList getHandlerList() { return HANDLER_LIST; }
}
