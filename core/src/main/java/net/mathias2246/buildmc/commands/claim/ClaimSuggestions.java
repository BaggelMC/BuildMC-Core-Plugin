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
        List<Long> claimIds = new ArrayList<>();

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

        String remaining = builder.getRemainingLowerCase();

        List<Long> finalClaimIds = claimIds;
        return CompletableFuture.supplyAsync(() -> {
            for (long id : finalClaimIds) {
                String name = ClaimManager.getClaimNameById(id);
                if (name != null && name.toLowerCase().startsWith(remaining)) {
                    builder.suggest(name);
                }
            }
            return builder.build();
        });
    }

    public static CompletableFuture<Suggestions> claimPlayerWhitelistSuggestions(
            @NotNull Player player,
            String type,
            String claimName,
            boolean isRemove,
            SuggestionsBuilder builder
    ) {
        Team team = null;
        if (type.equalsIgnoreCase("team")) {
            team = ClaimManager.getPlayerTeam(player);
        }

        List<Long> claimIds = switch (type.toLowerCase()) {
            case "player" -> ClaimManager.playerOwner.getOrDefault(player.getUniqueId(), List.of());
            case "team" -> team != null
                    ? ClaimManager.teamOwner.getOrDefault(team.getName(), List.of())
                    : List.of();
            case "server" -> player.hasPermission("buildmc.admin")
                    ? ClaimManager.serverClaims
                    : List.of();
            default -> List.of();
        };

        Claim claim = null;
        for (long id : claimIds) {
            if (claimName.equalsIgnoreCase(ClaimManager.getClaimNameById(id))) {
                claim = ClaimManager.getClaimByID(id);
                break;
            }
        }

        if (claim == null) {
            return builder.buildFuture();
        }

        record PlayerSnapshot(UUID uuid, String name) {}
        List<PlayerSnapshot> onlinePlayers = Bukkit.getOnlinePlayers().stream()
                .map(p -> new PlayerSnapshot(p.getUniqueId(), p.getName()))
                .toList();

        boolean isServer = claim.getType() == ClaimType.SERVER;
        String remaining = builder.getRemainingLowerCase();

        Team finalTeam = team;
        Claim finalClaim = claim;

        return CompletableFuture.supplyAsync(() -> {
            List<UUID> whitelist = finalClaim.getWhitelistedPlayers();

            for (PlayerSnapshot p : onlinePlayers) {
                boolean isWhitelisted = whitelist.contains(p.uuid());

                if (isRemove && !isWhitelisted) continue;
                if (!isRemove && isWhitelisted) continue;

                if (!isServer && p.uuid().equals(player.getUniqueId())) continue;

                if (!isRemove && type.equalsIgnoreCase("team") && finalTeam != null) {
                    if (finalTeam.hasEntry(p.name())) continue;
                }

                if (p.name().toLowerCase().startsWith(remaining)) {
                    builder.suggest(p.name());
                }
            }

            return builder.build();
        });
    }
}
