package net.mathias2246.buildmc.api.event.claims;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerEnterClaimEvent extends PlayerEvent {

    private static final @NotNull HandlerList HANDLERS = new HandlerList();

    public final @Nullable Long fromClaim;
    public final @Nullable Long toClaim;

    public PlayerEnterClaimEvent(@NotNull Player who, @Nullable Long fromClaimId, @Nullable Long toClaimId) {
        super(who);
        this.fromClaim = fromClaimId;
        this.toClaim = toClaimId;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
