package net.mathias2246.buildmc.commands.claim;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.api.event.claims.ClaimProtectionChangeEvent;
import net.mathias2246.buildmc.claims.ClaimLogger;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ClaimProtections {
    
    public static int changeClaimProtections(@NotNull Player player, @NotNull NamespacedKey flag, String valueStr, String type, String name) {

        type = type.toLowerCase();
        valueStr = valueStr.toLowerCase();

        // Parse value safely
        boolean enable;
        if (valueStr.equals("true")) {
            enable = true;
        } else if (valueStr.equals("false")) {
            enable = false;
        } else {
            CoreMain.plugin.sendMessage(player, Component.translatable("messages.claims.protections.invalid-value"));
            return 0;
        }

        if (Protection.isHiddenProtection(CoreMain.protectionsRegistry, flag)) {
            CoreMain.plugin.sendMessage(player, Component.translatable("messages.claims.protections.invalid-flag"));
            return 0;
        }

        Claim claim = null;
        long claimId = -1;

        switch (type) {
            case "player" -> {
                List<Long> ids = ClaimManager.playerOwner.getOrDefault(player.getUniqueId(), List.of());
                for (long id : ids) {
                    if (name.equalsIgnoreCase(ClaimManager.getClaimNameById(id))) {
                        claim = ClaimManager.getClaimByID(id);
                        claimId = id;
                        break;
                    }
                }
            }
            case "team" -> {
                Team team = ClaimManager.getPlayerTeam(player);
                if (team == null) {
                    CoreMain.plugin.sendMessage(player, Component.translatable("messages.error.not-in-a-team"));
                    return 0;
                }
                List<Long> ids = ClaimManager.teamOwner.getOrDefault(team.getName(), List.of());
                for (long id : ids) {
                    if (name.equalsIgnoreCase(ClaimManager.getClaimNameById(id))) {
                        claim = ClaimManager.getClaimByID(id);
                        claimId = id;
                        break;
                    }
                }
            }
            case "server" -> {
                if (!player.hasPermission("buildmc.admin")) {
                    CoreMain.plugin.sendMessage(player, Component.translatable("messages.error.no-permission"));
                    return 0;
                }
                for (long id : ClaimManager.serverClaims) {
                    if (name.equalsIgnoreCase(ClaimManager.getClaimNameById(id))) {
                        claim = ClaimManager.getClaimByID(id);
                        claimId = id;
                        break;
                    }
                }
            }
            default -> {
                CoreMain.plugin.sendMessage(player, Component.translatable("messages.claims.protections.invalid-type"));
                return 0;
            }
        }

        if (claim == null || claimId == -1) {
            CoreMain.plugin.sendMessage(player, Component.translatable("messages.claims.remove.not-found"));
            return 0;
        }

        Protection protection = Objects.requireNonNull(CoreMain.protectionsRegistry.get(flag));

        ClaimProtectionChangeEvent event = new ClaimProtectionChangeEvent(
                claim,
                protection,
                enable ? ClaimProtectionChangeEvent.ActiveState.ENABLED : ClaimProtectionChangeEvent.ActiveState.DISABLED,
                player
        );
        if (event.isCancelled()) return 0;

        if (enable) {
            ClaimManager.addProtection(claimId, flag);
            CoreMain.plugin.sendMessage(player, Message.msg(player, "messages.claims.protections.added", Map.of("flag", flag.toString())));
            ClaimLogger.logProtectionChanged(player, name, flag.toString(), "enabled");
        } else {
            ClaimManager.removeProtection(claimId, flag);
            CoreMain.plugin.sendMessage(player, Message.msg(player, "messages.claims.protections.removed", Map.of("flag", flag.toString())));
            ClaimLogger.logProtectionChanged(player, name, flag.toString(), "disabled");
        }

        return 1;
    }
    
}
