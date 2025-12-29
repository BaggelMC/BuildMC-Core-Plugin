package net.mathias2246.buildmc.commands.claim;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.claims.ClaimType;
import net.mathias2246.buildmc.claims.ClaimManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ClaimSuggestions {

    public static CompletableFuture<Suggestions> claimTypesSuggestions(@NotNull Player player, SuggestionsBuilder builder) {
        List<String> suggestions = new ArrayList<>(List.of("player", "team"));

        // Add admin-only options
        if (player.hasPermission("buildmc.admin")) {
            suggestions.add("server");
            suggestions.add("placeholder");
        }

        for (String suggestion : suggestions) {
            if (suggestion.startsWith(builder.getRemaining().toLowerCase())) {
                builder.suggest(suggestion);
            }
        }
        return builder.buildFuture();
    }

    public static CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> claimIdsSuggestions(@NotNull Player player, String type, SuggestionsBuilder builder) {

        if (type.equalsIgnoreCase("player")) {
            List<Long> claimIds = ClaimManager.playerOwner.getOrDefault(player.getUniqueId(), List.of());
            for (long id : claimIds) {
                String name = ClaimManager.getClaimNameById(id);
                if (name != null && name.toLowerCase().startsWith(builder.getRemaining().toLowerCase())) {
                    builder.suggest(name);
                }
            }
        } else if (type.equalsIgnoreCase("team")) {
            Team team = ClaimManager.getPlayerTeam(player);
            if (team != null) {
                List<Long> claimIds = ClaimManager.teamOwner.getOrDefault(team.getName(), List.of());
                for (long id : claimIds) {
                    String name = ClaimManager.getClaimNameById(id);
                    if (name != null && name.toLowerCase().startsWith(builder.getRemaining().toLowerCase())) {
                        builder.suggest(name);
                    }
                }
            }
        } else if (type.equalsIgnoreCase("server")) {
            if (player.hasPermission("buildmc.admin")) {
                List<Long> claimIds = ClaimManager.serverClaims;
                for (long id : claimIds) {
                    String name = ClaimManager.getClaimNameById(id);
                    if (name != null && name.toLowerCase().startsWith(builder.getRemaining().toLowerCase())) {
                        builder.suggest(name);
                    }
                }
            }
        }

        return builder.buildFuture();
    }

    public static CompletableFuture<Suggestions> claimPlayerWhitelistSuggestions(@NotNull Player player, String type, String claimName, SuggestionsBuilder builder) {
        Claim claim = null;
        List<Long> claimIds = List.of();

        if (type.equalsIgnoreCase("player")) {
            claimIds = ClaimManager.playerOwner.getOrDefault(player.getUniqueId(), List.of());
        } else if (type.equalsIgnoreCase("team")) {
            Team team = ClaimManager.getPlayerTeam(player);
            if (team != null) {
                claimIds = ClaimManager.teamOwner.getOrDefault(team.getName(), List.of());
            }
        } else if (type.equalsIgnoreCase("server") && player.hasPermission("buildmc.admin")) {
            claimIds = ClaimManager.serverClaims;
        }

        for (long id : claimIds) {
            if (claimName.equalsIgnoreCase(ClaimManager.getClaimNameById(id))) {
                claim = ClaimManager.getClaimByID(id);
                break;
            }
        }

        if (claim == null) return builder.buildFuture();

        List<UUID> whitelist = claim.getWhitelistedPlayers();
        boolean isServer = claim.getType() == ClaimType.SERVER;

        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (offlinePlayer.getName() == null) continue;
            if (!isServer && offlinePlayer.getUniqueId().equals(player.getUniqueId())) continue;
            if (whitelist.contains(offlinePlayer.getUniqueId())) continue;
            if (type.equalsIgnoreCase("team")) {
                Team team = ClaimManager.getPlayerTeam(player);
                if (team != null && team.hasEntry(offlinePlayer.getName())) continue;
            }
            if (offlinePlayer.getName().startsWith(builder.getRemaining())) {
                builder.suggest(offlinePlayer.getName());
            }
        }

        return builder.buildFuture();
    }
}
