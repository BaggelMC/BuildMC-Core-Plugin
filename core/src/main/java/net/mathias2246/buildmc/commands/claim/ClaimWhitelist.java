package net.mathias2246.buildmc.commands.claim;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.event.claims.ClaimWhitelistChangeEvent;
import net.mathias2246.buildmc.claims.ClaimLogger;
import net.mathias2246.buildmc.claims.ClaimManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class ClaimWhitelist {

    public static int whitelistClaimCommand(@NotNull Player player, String type, String name, String action, String targetPlayerName) {
        List<Long> claimIds = switch (type.toLowerCase()) {
            case "player" -> ClaimManager.playerOwner.getOrDefault(player.getUniqueId(), List.of());
            case "team" -> {
                Team team = ClaimManager.getPlayerTeam(player);
                if (team == null) {
                    CoreMain.plugin.sendMessage(player, Component.translatable("messages.error.not-in-a-team"));
                    yield List.of();
                }
                yield ClaimManager.teamOwner.getOrDefault(team.getName(), List.of());
            }
            case "server" -> player.hasPermission("buildmc.admin") ? ClaimManager.serverClaims : List.of();
            default -> {
                CoreMain.plugin.sendMessage(player, Component.translatable("messages.claims.create.invalid-type"));
                yield List.of();
            }
        };

        Long claimId = claimIds.stream()
                .filter(id -> name.equalsIgnoreCase(ClaimManager.getClaimNameById(id)))
                .findFirst()
                .orElse(null);

        if (claimId == null) {
            CoreMain.plugin.sendMessage(player, Component.translatable("messages.claims.whitelist.claim-not-found"));
            return 0;
        }

        Player target = Bukkit.getPlayerExact(targetPlayerName);
        if (target == null) {
            CoreMain.plugin.sendMessage(player, Component.translatable("messages.claims.whitelist.player-not-found"));
            return 0;
        }

        UUID targetUUID = target.getUniqueId();
        Claim claim = ClaimManager.getClaimByID(claimId);
        if (claim == null) {
            CoreMain.plugin.sendMessage(player, Component.translatable("messages.claims.whitelist.claim-not-found"));
            return 0;
        }

        switch (action.toLowerCase()) {
            case "add" -> {
                if (claim.getWhitelistedPlayers().contains(targetUUID)) {
                    CoreMain.plugin.sendMessage(player, Component.translatable("messages.claims.whitelist.already-added"));
                    return 0;
                }

                ClaimWhitelistChangeEvent event = new ClaimWhitelistChangeEvent(
                        claim,
                        target,
                        player,
                        ClaimWhitelistChangeEvent.ChangeAction.ADDED
                );
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) return 0;


                ClaimManager.addPlayerToWhitelist(claimId, targetUUID);
                CoreMain.plugin.sendMessage(player, Component.translatable("messages.claims.whitelist.added"));
                ClaimLogger.logWhitelistAdded(player, name, targetPlayerName, targetUUID.toString());
                return 1;
            }
            case "remove" -> {
                if (!claim.getWhitelistedPlayers().contains(targetUUID)) {
                    CoreMain.plugin.sendMessage(player, Component.translatable("messages.claims.whitelist.not-on-list"));
                    return 0;
                }

                ClaimWhitelistChangeEvent event = new ClaimWhitelistChangeEvent(
                        claim,
                        target,
                        player,
                        ClaimWhitelistChangeEvent.ChangeAction.REMOVED
                );
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) return 0;

                ClaimManager.removePlayerFromWhitelist(claimId, targetUUID);
                CoreMain.plugin.sendMessage(player, Component.translatable("messages.claims.whitelist.removed"));
                ClaimLogger.logWhitelistRemoved(player, name, targetPlayerName, targetUUID.toString());
                return 1;
            }
            default -> {
                CoreMain.plugin.sendMessage(player, Component.translatable("messages.claims.whitelist.invalid-action"));
                return 0;
            }
        }
    }

}
