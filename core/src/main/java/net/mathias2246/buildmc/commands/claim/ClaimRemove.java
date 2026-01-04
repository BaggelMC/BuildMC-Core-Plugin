package net.mathias2246.buildmc.commands.claim;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.claims.ClaimLogger;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.util.LocationUtil;
import net.mathias2246.buildmc.util.SoundUtil;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClaimRemove {
    
    public static int removeClaimCommand(@NotNull Player player, String type, String claimName) {
        long claimId = -1;
        switch (type.toLowerCase()) {
            case "player" -> {
                List<Long> ids = ClaimManager.playerOwner.getOrDefault(player.getUniqueId(), List.of());
                for (long id : ids) {
                    String name = ClaimManager.getClaimNameById(id);
                    if (name != null && name.equalsIgnoreCase(claimName)) {
                        claimId = id;
                        break;
                    }
                }
            }
            case "team" -> {
                Team team = ClaimManager.getPlayerTeam(player);
                if (team == null) {
                    CoreMain.plugin.sendMessage(player, Component.translatable("messages.error.not-in-a-team"));
                    CoreMain.soundManager.playSound(player, SoundUtil.mistake);
                    return 0;
                }
                List<Long> ids = ClaimManager.teamOwner.getOrDefault(team.getName(), List.of());
                for (long id : ids) {
                    String name = ClaimManager.getClaimNameById(id);
                    if (name != null && name.equalsIgnoreCase(claimName)) {
                        claimId = id;
                        break;
                    }
                }
            }
            case "server", "placeholder" -> {
                if (!player.hasPermission("buildmc.admin")) {
                    CoreMain.plugin.sendMessage(player, Component.translatable("messages.error.no-permission"));
                    CoreMain.soundManager.playSound(player, SoundUtil.mistake);
                    return 0;
                }
                List<Long> ids = type.equals("server") ? ClaimManager.serverClaims : ClaimManager.placeholderClaims;
                for (long id : ids) {
                    String name = ClaimManager.getClaimNameById(id);
                    if (name != null && name.equalsIgnoreCase(claimName)) {
                        claimId = id;
                        break;
                    }
                }
            }
            default -> {
                CoreMain.plugin.sendMessage(player, Component.translatable("messages.claims.remove.invalid-type"));
                CoreMain.soundManager.playSound(player, SoundUtil.mistake);
                return 0;
            }
        }

        if (claimId == -1) {
            CoreMain.plugin.sendMessage(player, Component.translatable("messages.claims.remove.not-found"));
            CoreMain.soundManager.playSound(player, SoundUtil.mistake);
            return 0;
        }

        Claim claim = ClaimManager.getClaimByID(claimId);
        if (claim == null) {
            CoreMain.plugin.sendMessage(player, Component.translatable("messages.claims.remove.failed"));
            CoreMain.soundManager.playSound(player, SoundUtil.mistake);
            return 0;
        }

        int restoredChunks = LocationUtil.calculateChunkArea(claim.getChunkX1(), claim.getChunkZ1(), claim.getChunkX2(), claim.getChunkZ2());

        switch (claim.getType()) {
            case PLAYER -> {
                String ownerUUID = claim.getOwnerId();
                int max = CoreMain.plugin.getConfig().getInt("claims.player-max-chunk-claim-amount");
                int current = ClaimManager.playerRemainingClaims.getOrDefault(ownerUUID, max);
                ClaimManager.playerRemainingClaims.put(ownerUUID, current + restoredChunks);
            }
            case TEAM -> {
                String teamName = claim.getOwnerId();
                int max = CoreMain.plugin.getConfig().getInt("claims.team-max-chunk-claim-amount");
                int current = ClaimManager.teamRemainingClaims.getOrDefault(teamName, max);
                ClaimManager.teamRemainingClaims.put(teamName, current + restoredChunks);
            }
        }

        boolean success = ClaimManager.removeClaimById(claimId);

        if (success) {
            CoreMain.plugin.sendMessage(player, Component.translatable("messages.claims.remove.success"));
            CoreMain.soundManager.playSound(player, SoundUtil.success);
            ClaimLogger.logClaimDeleted(player, claimName);
            return 1;
        }
        CoreMain.plugin.sendMessage(player, Component.translatable("messages.claims.remove.failed"));
        CoreMain.soundManager.playSound(player, SoundUtil.mistake);
        return 0;
    }
    
}
