package net.mathias2246.buildmc.claims;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.claims.ClaimType;
import net.mathias2246.buildmc.api.event.claims.PlayerChangeClaimEvent;
import net.mathias2246.buildmc.util.AudienceUtil;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class ClaimActionBarHandler implements Listener {
    public static final @NotNull NamespacedKey ACTION_BAR_TOGGLED_KEY = new NamespacedKey("buildmc", "show_claim_action_bar");

    private static final @NotNull PlainTextComponentSerializer SERIALIZER = PlainTextComponentSerializer.builder().build();

    public static boolean hasActionBarMessageEnabled(@NotNull Player player) {
        var val = player.getPersistentDataContainer().get(ACTION_BAR_TOGGLED_KEY, PersistentDataType.BOOLEAN);
        if (val == null) return true;
        return val;
    }

    public static void setPlayerEnabled(@NotNull Player player, boolean enabled) {
        player.getPersistentDataContainer().set(ACTION_BAR_TOGGLED_KEY, PersistentDataType.BOOLEAN, enabled);
    }

    @EventHandler
    public void onClaimBoundaryCross(PlayerChangeClaimEvent event) {
        Player player = event.getPlayer();
        if (!hasActionBarMessageEnabled(player)) return;

        Claim claim = ClaimManager.getClaimByID(event.getToClaim());
        Claim previousClaim = ClaimManager.getClaimByID(event.getFromClaim());

        if (
                (claim == null || // If the new claim is null
                claim.getType() == ClaimType.PLACEHOLDER || // OR a placeholder claim
                claim.getType() == ClaimType.SERVER) && // OR a server claim
                        // But don't show a message if the player is leaving a placeholder or server claim
                        !(previousClaim == null || (previousClaim.getType() == ClaimType.PLACEHOLDER || previousClaim.getType() == ClaimType.SERVER))
        ) {
            AudienceUtil.sendActionBar(
                    player,
                    Message.msg(player, "messages.claims.move.leave-claim")
            );
            return;
        }

        if (claim == null) return;

        String ownerTypeStr;
        String ownerName;
        TextColor color = NamedTextColor.WHITE;

        switch (claim.getType()) {
            case PLACEHOLDER, SERVER:
                return;
            case PLAYER:
                ownerTypeStr = Message.msgStr(player, "messages.claims.ui.create-dialog.claim-types.player");

                UUID ownerId = UUID.fromString(claim.getOwnerId());
                OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerId);
                ownerName = owner.getName();

                if (ownerName == null) {
                    ownerName = Message.msgStr(player, "messages.claims.move.unknown-owner");
                }
                break;
            case TEAM:
                ownerTypeStr = Message.msgStr(player, "messages.claims.ui.create-dialog.claim-types.team");

                var team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(claim.getOwnerId());
                if (team == null) {
                    ownerName = "?";
                    break;
                }

                try {
                    color = team.color();
                } catch (Exception ignore) { }
                try {
                    ownerName = SERIALIZER.serialize(team.displayName());
                } catch (Exception ignore) { ownerName = Message.msgStr(player, "messages.claims.move.unknown-owner"); }
                break;
            default:
                ownerTypeStr = Message.msgStr(player, "messages.claims.move.unknown-owner-type");
                ownerName = Message.msgStr(player, "messages.claims.move.unknown-owner");
        }

        AudienceUtil.sendActionBar(
                player,
                Message.msg(
                        player,
                        "messages.claims.move.enter-claim",
                        Map.of(
                                "owner-type", ownerTypeStr,
                                "owner", ownerName
                        )
                ).color(color)
        );
    }

}
