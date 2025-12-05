package net.mathias2246.buildmc.commands.claim;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.claims.ClaimType;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public class ClaimWho {

    public static int whoClaimCommand(@NotNull Player player, @NotNull Location location) {
        Claim claim;

        try {
            claim = ClaimManager.getClaim(location);
        } catch (SQLException e) {
            CoreMain.plugin.getLogger().severe("An error occurred while getting a claim from the database: " + e.getMessage());
            CoreMain.plugin.sendMessage(player, Component.translatable("messages.error.sql"));
            return 0;
        }

        if (claim == null) {
            CoreMain.plugin.sendMessage(player, Component.translatable("messages.claims.who.unclaimed"));
            return 1;
        }

        ClaimType claimType = claim.getType();

        if (claimType == ClaimType.TEAM) {
            CoreMain.plugin.sendMessage(player, Message.msg(player, "messages.claims.who.team-message", Map.of("owner", claim.getOwnerId())));
        } else if (claimType == ClaimType.PLAYER) {
            UUID ownerId = UUID.fromString(claim.getOwnerId());
            OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerId);
            String ownerName = owner.getName();

            if (ownerName == null) {
                ownerName = "Unknown";
            }

            CoreMain.plugin.sendMessage(player, Message.msg(player, "messages.claims.who.player-message", Map.of("owner", ownerName)));
        } else if (claimType == ClaimType.SERVER || claimType == ClaimType.PLACEHOLDER) {
            CoreMain.plugin.sendMessage(player, Message.msg(player, "messages.claims.who.server-message"));
        }

        return 1;
    }

}
