package net.mathias2246.buildmc.claims;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.Main;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.claims.ClaimType;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.claims.tools.ClaimSelectionTool;
import net.mathias2246.buildmc.commands.CustomCommand;
import net.mathias2246.buildmc.ui.claims.ClaimSelectMenu;
import net.mathias2246.buildmc.util.DeferredRegistry;
import net.mathias2246.buildmc.util.LocationUtil;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.*;

public class ClaimCommand implements CustomCommand {

    private static final @NotNull ClaimToolItemMetaModifier claimToolNameAndTooltip = new ClaimToolItemMetaModifier();
    private static ClaimSelectionTool claimTool;

    @Override
    public LiteralCommandNode<CommandSourceStack> getCommand() {

        DeferredRegistry<Protection> protectionsReg = CoreMain.protectionsRegistry;

        claimTool = (ClaimSelectionTool) Main.customItems.get(Objects.requireNonNull(NamespacedKey.fromString("buildmc:claim_tool")).key());

        var cmd = Commands.literal("claim");

        cmd.executes((command) -> {
            if (!(command.getSource().getExecutor() instanceof Player player)) {
                command.getSource().getSender().sendMessage(Component.translatable("messages.error.not-a-player"));
                return 0;
            }

            ClaimSelectMenu.open(player);
            return 1;
        });

        cmd.then(
                Commands.literal("claimtool")
                .executes(
                        (command) -> {
                            if (!(command.getSource().getExecutor() instanceof Player player)) {
                                command.getSource().getSender().sendMessage(Component.translatable("messages.error.not-a-player"));
                                return 0;
                            }

                            // Check if there is space left in the inventory
                            if (player.getInventory().firstEmpty() == -1) {
                                player.sendMessage(Component.translatable("messages.claims.tool.full-inventory"));
                                return 0;
                            }

                            claimTool.giveToPlayer(player, claimToolNameAndTooltip);
                            player.sendMessage(Component.translatable("messages.claims.tool.give-success"));
                            return 1;
                        })
            );
        cmd.then(
                Commands.literal("edit")
                        .executes(
                                (command) -> {
                                    if (!(command.getSource().getExecutor() instanceof Player player)) {
                                        command.getSource().getSender().sendMessage(Component.translatable("messages.error.not-a-player"));
                                        return 0;
                                    }

                                    ClaimSelectMenu.open(player);
                                    return 1;
                                })
        );
        cmd.then(
                Commands.literal("who")
                        .executes(
                                (command) -> {
                                    if (!(command.getSource().getSender() instanceof Player player)) {
                                        command.getSource().getSender().sendMessage(Component.translatable("messages.error.not-a-player"));
                                        return 0;
                                    }

                                    Claim claim;

                                    try {
                                        claim = ClaimManager.getClaim(player.getLocation());
                                    } catch (SQLException e) {
                                        CoreMain.plugin.getLogger().severe("An error occurred while getting a claim from the database: " + e.getMessage());
                                        player.sendMessage(Component.translatable("messages.error.sql"));
                                        return 0;
                                    }

                                    if (claim == null) {
                                        command.getSource().getSender().sendMessage(Component.translatable("messages.claims.who.unclaimed"));
                                        return 1;
                                    }

                                    ClaimType claimType = claim.getType();

                                    if (claimType == ClaimType.TEAM) {
                                        player.sendMessage(Message.msg(player, "messages.claims.who.team-message", Map.of("owner", claim.getOwnerId())));
                                    } else if (claimType == ClaimType.PLAYER) {
                                        UUID ownerId = UUID.fromString(claim.getOwnerId());
                                        OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerId);
                                        String ownerName = owner.getName();

                                        if (ownerName == null) {
                                            ownerName = "Unknown";
                                        }

                                        player.sendMessage(Message.msg(player, "messages.claims.who.player-message", Map.of("owner", ownerName)));
                                    } else if (claimType == ClaimType.SERVER || claimType == ClaimType.PLACEHOLDER) {
                                        player.sendMessage(Message.msg(player, "messages.claims.who.server-message"));
                                    }

                                    return 1;
                                }
                        )
        );
        cmd.then(
                Commands.literal("help")
                        .executes(command -> {
                            var sender = command.getSource().getSender();
                            sender.sendMessage(Component.translatable("messages.claims.help-message"));
                            return 1;
                        })
        );

        cmd.then(
                Commands.literal("create")
                        .then(
                                Commands.argument("type", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            List<String> suggestions = new ArrayList<>(List.of("player", "team"));

                                            // Add admin-only options
                                            if (context.getSource().getSender() instanceof Player player &&
                                                    player.hasPermission("buildmc.admin")) {
                                                suggestions.add("server");
                                                suggestions.add("placeholder");
                                            }

                                            for (String suggestion : suggestions) {
                                                if (suggestion.startsWith(builder.getRemaining().toLowerCase())) {
                                                    builder.suggest(suggestion);
                                                }
                                            }
                                            return builder.buildFuture();
                                        })
                                        .then(
                                                Commands.argument("name", StringArgumentType.word()) // name of claim
                                                        .executes(command -> {
                                                            if (!(command.getSource().getSender() instanceof Player player)) {
                                                                command.getSource().getSender().sendMessage(Component.translatable("messages.error.not-a-player"));
                                                                return 0;
                                                            }

                                                            String type = command.getArgument("type", String.class);
                                                            String name = command.getArgument("name", String.class);

                                                            // Validate positions

                                                            if (!player.hasMetadata(claimTool.firstSelectionKey) || !player.hasMetadata(claimTool.secondSelectionKey)) {
                                                                player.sendMessage(Component.translatable("messages.claims.create.missing-positions"));
                                                                return 0;
                                                            }

                                                            Location pos1 = claimTool.getFirstSelection(player);
                                                            Location pos2 = claimTool.getSecondSelection(player);

                                                            if (pos1 == null || pos2 == null) {
                                                                player.sendMessage(Component.translatable("messages.claims.create.missing-positions"));
                                                                return 0;
                                                            }

                                                            if (!pos1.getWorld().equals(pos2.getWorld())) {
                                                                player.sendMessage(Component.translatable("messages.claims.create.different-worlds"));
                                                                return 0;
                                                            }

                                                            List<Claim> overlappingClaims;
                                                            try {
                                                                overlappingClaims = ClaimManager.getClaimsInArea(pos1, pos2);
                                                            } catch (SQLException e) {
                                                                CoreMain.plugin.getLogger().severe("There was an error while getting the claims in an area: " + e.getMessage());
                                                                player.sendMessage(Component.translatable("messages.claims.create.error-database"));
                                                                return 0;
                                                            }

                                                            if (!overlappingClaims.isEmpty()) {
                                                                boolean serverProtected = overlappingClaims.stream()
                                                                        .anyMatch(claim -> claim.getType() == ClaimType.SERVER);

                                                                if (serverProtected) {
                                                                    player.sendMessage(Component.translatable("messages.claims.create.protected-server"));
                                                                } else {
                                                                    player.sendMessage(Component.translatable("messages.claims.create.overlap"));
                                                                }
                                                                return 0;
                                                            }

                                                            int newClaimChunks = LocationUtil.calculateChunkArea(pos1, pos2);

                                                            switch (type.toLowerCase()) {
                                                                case "player" -> {
                                                                    // Check remaining claims for player
                                                                    int maxChunksAllowed = CoreMain.plugin.getConfig().getInt("claims.player-max-chunk-claim-amount");
                                                                    int currentClaimedChunks = ClaimManager.playerRemainingClaims.getOrDefault(player.getUniqueId().toString(), maxChunksAllowed);
                                                                    if ((currentClaimedChunks - newClaimChunks) <= 0) {
                                                                        player.sendMessage(Message.msg(player, "messages.claims.create.no-remaining-claims", Map.of("no-remaining-claims", String.valueOf(currentClaimedChunks))));
                                                                        return 0;
                                                                    }

                                                                    try {
                                                                        if (ClaimManager.doesOwnerHaveClaimWithName(player.getUniqueId().toString(), name)) {
                                                                            player.sendMessage(Component.translatable("messages.claims.create.duplicate-name"));
                                                                            return 1;
                                                                        }
                                                                    } catch (SQLException e) {
                                                                        CoreMain.plugin.getLogger().severe("SQL Error while trying to check claim name availability: " + e);
                                                                        player.sendMessage(Component.translatable("messages.claims.create.error-database"));
                                                                        return 0;
                                                                    }

                                                                    boolean success = ClaimManager.tryClaimPlayerArea(player, name, pos1, pos2);
                                                                    if (success) {
                                                                        player.sendMessage(Message.msg(player, "messages.claims.create.success", Map.of("remaining_claims", String.valueOf(currentClaimedChunks - newClaimChunks))));
                                                                        player.removeMetadata(claimTool.firstSelectionKey, claimTool.getPlugin());
                                                                        player.removeMetadata(claimTool.secondSelectionKey, claimTool.getPlugin());
                                                                        return 1;
                                                                    } else {
                                                                        player.sendMessage(Component.translatable("messages.claims.create.failed"));
                                                                        return 0;
                                                                    }
                                                                }

                                                                case "team" -> {
                                                                    Team team = ClaimManager.getPlayerTeam(player);
                                                                    if (team == null) {
                                                                        player.sendMessage(Component.translatable("messages.error.not-in-a-team"));
                                                                        return 0;
                                                                    }

                                                                    // Check remaining claims for team
                                                                    int maxChunksAllowed = CoreMain.plugin.getConfig().getInt("claims.team-max-chunk-claim-amount");
                                                                    int currentClaimedChunks = ClaimManager.teamRemainingClaims.getOrDefault(team.getName(), maxChunksAllowed);
                                                                    if ((currentClaimedChunks - newClaimChunks) <= 0) {
                                                                        player.sendMessage(Message.msg(player, "messages.claims.create.no-remaining-claims", Map.of("remaining_claims", String.valueOf(currentClaimedChunks))));
                                                                        return 0;
                                                                    }

                                                                    try {
                                                                        if (ClaimManager.doesOwnerHaveClaimWithName(team.getName(), name)) {
                                                                            player.sendMessage(Component.translatable("messages.claims.create.duplicate-name"));
                                                                            return 1;
                                                                        }
                                                                    } catch (SQLException e) {
                                                                        CoreMain.plugin.getLogger().severe("SQL Error while trying to check claim name availability: " + e);
                                                                        player.sendMessage(Component.translatable("messages.claims.create.error-database"));
                                                                        return 0;
                                                                    }

                                                                    boolean success = ClaimManager.tryClaimTeamArea(team, name, pos1, pos2);
                                                                    if (success) {
                                                                        player.sendMessage(Message.msg(player, "messages.claims.create.success", Map.of("remaining_claims", String.valueOf(currentClaimedChunks - newClaimChunks))));
                                                                        player.removeMetadata(claimTool.firstSelectionKey, claimTool.getPlugin());
                                                                        player.removeMetadata(claimTool.secondSelectionKey, claimTool.getPlugin());
                                                                        return 1;
                                                                    } else {
                                                                        player.sendMessage(Component.translatable("messages.claims.create.failed"));
                                                                        return 0;
                                                                    }
                                                                }

                                                                case "server", "placeholder" -> {
                                                                    if (!player.hasPermission("buildmc.admin")) {
                                                                        player.sendMessage(Component.translatable("messages.error.no-permission"));
                                                                        return 0;
                                                                    }

                                                                    try {
                                                                        if (ClaimManager.doesOwnerHaveClaimWithName(type.toLowerCase(), name)) {
                                                                            player.sendMessage(Component.translatable("messages.claims.create.duplicate-name"));
                                                                            return 1;
                                                                        }
                                                                    } catch (SQLException e) {
                                                                        CoreMain.plugin.getLogger().severe("SQL Error while trying to check claim name availability: " + e);
                                                                        player.sendMessage(Component.translatable("messages.claims.create.error-database"));
                                                                        return 0;
                                                                    }

                                                                    boolean success = switch (type.toLowerCase()) {
                                                                        case "server" -> ClaimManager.tryClaimServerArea(name, pos1, pos2);
                                                                        case "placeholder" -> ClaimManager.tryClaimPlaceholderArea(name, pos1, pos2);
                                                                        default -> false;
                                                                    };

                                                                    if (success) {
                                                                        player.sendMessage(Message.msg(player,
                                                                                "messages.claims.create.success-" + type.toLowerCase(),
                                                                                Map.of("claim_name", name)
                                                                        ));
                                                                        player.removeMetadata(claimTool.firstSelectionKey, claimTool.getPlugin());
                                                                        player.removeMetadata(claimTool.secondSelectionKey, claimTool.getPlugin());
                                                                        return 1;
                                                                    } else {
                                                                        player.sendMessage(Component.translatable("messages.claims.create.failed"));
                                                                        return 0;
                                                                    }
                                                                }

                                                                default -> {
                                                                    player.sendMessage(Component.translatable("messages.claims.create.invalid-type"));
                                                                    return 0;
                                                                }
                                                            }
                                                        })
                                        )
                        )
        );

        cmd.then(
                Commands.literal("remove")
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    List<String> types = List.of("player", "team");
                                    for (String type : types) {
                                        if (type.startsWith(builder.getRemaining().toLowerCase())) {
                                            builder.suggest(type);
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("claim", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            var sender = ctx.getSource().getSender();
                                            if (!(sender instanceof Player player)) return builder.buildFuture();

                                            String type = StringArgumentType.getString(ctx, "type").toLowerCase();

                                            if (type.equals("player")) {
                                                List<Long> claimIds = ClaimManager.playerOwner.getOrDefault(player.getUniqueId(), List.of());
                                                for (long id : claimIds) {
                                                    String name = ClaimManager.getClaimNameById(id);
                                                    if (name != null && name.toLowerCase().startsWith(builder.getRemaining().toLowerCase())) {
                                                        builder.suggest(name);
                                                    }
                                                }
                                            } else if (type.equals("team")) {
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
                                            }

                                            return builder.buildFuture();
                                        })
                                        .executes(ctx -> {
                                            var sender = ctx.getSource().getSender();
                                            if (!(sender instanceof Player player)) {
                                                sender.sendMessage(Component.translatable("messages.error.not-a-player"));
                                                return 0;
                                            }

                                            String type = StringArgumentType.getString(ctx, "type").toLowerCase();
                                            String claimName = StringArgumentType.getString(ctx, "claim");

                                            long claimId = -1;

                                            switch (type) {
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
                                                        player.sendMessage(Component.translatable("messages.error.not-in-a-team"));
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
                                                default -> {
                                                    player.sendMessage(Component.translatable("messages.claims.remove.invalid-type"));
                                                    return 0;
                                                }
                                            }

                                            if (claimId == -1) {
                                                player.sendMessage(Component.translatable("messages.claims.remove.not-found"));
                                                return 0;
                                            }

                                            boolean success = ClaimManager.removeClaimById(claimId);
                                            if (success) {
                                                player.sendMessage(Component.translatable("messages.claims.remove.success"));
                                            } else {
                                                player.sendMessage(Component.translatable("messages.claims.remove.failed"));
                                            }

                                            return success ? 1 : 0;
                                        })
                                )
                        )
        );

        cmd.then(
                Commands.literal("whitelist")
                        .then(Commands.argument("action", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    List<String> actions = List.of("add", "remove");
                                    for (String action : actions) {
                                        if (action.startsWith(builder.getRemaining())) {
                                            builder.suggest(action);
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            List<String> types = List.of("player", "team");
                                            for (String type : types) {
                                                if (type.startsWith(builder.getRemaining())) {
                                                    builder.suggest(type);
                                                }
                                            }
                                            return builder.buildFuture();
                                        })
                                        .then(Commands.argument("claim", StringArgumentType.word())
                                                .suggests((ctx, builder) -> {
                                                    var sender = ctx.getSource().getSender();
                                                    if (!(sender instanceof Player player)) return builder.buildFuture();
                                                    String type = StringArgumentType.getString(ctx, "type");

                                                    if (type.equalsIgnoreCase("player")) {
                                                        List<Long> claimIds = ClaimManager.playerOwner.getOrDefault(player.getUniqueId(), List.of());
                                                        for (long id : claimIds) {
                                                            String name = ClaimManager.getClaimNameById(id);
                                                            if (name != null && name.startsWith(builder.getRemaining())) {
                                                                builder.suggest(name);
                                                            }
                                                        }
                                                    } else if (type.equalsIgnoreCase("team")) {
                                                        Team team = ClaimManager.getPlayerTeam(player);
                                                        if (team != null) {
                                                            List<Long> claimIds = ClaimManager.teamOwner.getOrDefault(team.getName(), List.of());
                                                            for (long id : claimIds) {
                                                                String name = ClaimManager.getClaimNameById(id);
                                                                if (name != null && name.startsWith(builder.getRemaining())) {
                                                                    builder.suggest(name);
                                                                }
                                                            }
                                                        }
                                                    }

                                                    return builder.buildFuture();
                                                })
                                                .then(Commands.argument("player", StringArgumentType.word())
                                                        .suggests((ctx, builder) -> {
                                                            var sender = ctx.getSource().getSender();
                                                            if (!(sender instanceof Player player)) return builder.buildFuture();
                                                            String type = StringArgumentType.getString(ctx, "type");
                                                            String claimName = StringArgumentType.getString(ctx, "claim");

                                                            Claim claim = null;
                                                            if (type.equalsIgnoreCase("player")) {
                                                                List<Long> ids = ClaimManager.playerOwner.getOrDefault(player.getUniqueId(), List.of());
                                                                for (long id : ids) {
                                                                    if (claimName.equalsIgnoreCase(ClaimManager.getClaimNameById(id))) {
                                                                        claim = ClaimManager.getClaimByID(id);
                                                                        break;
                                                                    }
                                                                }
                                                            } else if (type.equalsIgnoreCase("team")) {
                                                                Team team = ClaimManager.getPlayerTeam(player);
                                                                if (team != null) {
                                                                    List<Long> ids = ClaimManager.teamOwner.getOrDefault(team.getName(), List.of());
                                                                    for (long id : ids) {
                                                                        if (claimName.equalsIgnoreCase(ClaimManager.getClaimNameById(id))) {
                                                                            claim = ClaimManager.getClaimByID(id);
                                                                            break;
                                                                        }
                                                                    }
                                                                }
                                                            }

                                                            if (claim == null) return builder.buildFuture();

                                                            List<UUID> whitelist = claim.getWhitelistedPlayers();
                                                            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                                                                if (offlinePlayer.getName() == null) continue;
                                                                if (offlinePlayer.getUniqueId().equals(player.getUniqueId())) continue;
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
                                                        })
                                                        .executes(ctx -> {
                                                            var sender = ctx.getSource().getSender();
                                                            if (!(sender instanceof Player player)) {
                                                                sender.sendMessage(Component.translatable("messages.error.not-a-player"));
                                                                return 0;
                                                            }

                                                            String action = StringArgumentType.getString(ctx, "action").toLowerCase();
                                                            String type = StringArgumentType.getString(ctx, "type").toLowerCase();
                                                            String claimName = StringArgumentType.getString(ctx, "claim");
                                                            String targetPlayerName = StringArgumentType.getString(ctx, "player");

                                                            long claimId = -1;
                                                            Claim claim = null;

                                                            switch (type) {
                                                                case "player" -> {
                                                                    List<Long> ids = ClaimManager.playerOwner.getOrDefault(player.getUniqueId(), List.of());
                                                                    for (long id : ids) {
                                                                        if (claimName.equalsIgnoreCase(ClaimManager.getClaimNameById(id))) {
                                                                            claim = ClaimManager.getClaimByID(id);
                                                                            claimId = id;
                                                                            break;
                                                                        }
                                                                    }
                                                                }

                                                                case "team" -> {
                                                                    Team team = ClaimManager.getPlayerTeam(player);
                                                                    if (team == null) {
                                                                        player.sendMessage(Component.translatable("messages.error.not-in-a-team"));
                                                                        return 0;
                                                                    }
                                                                    List<Long> ids = ClaimManager.teamOwner.getOrDefault(team.getName(), List.of());
                                                                    for (long id : ids) {
                                                                        if (claimName.equalsIgnoreCase(ClaimManager.getClaimNameById(id))) {
                                                                            claim = ClaimManager.getClaimByID(id);
                                                                            claimId = id;
                                                                            break;
                                                                        }
                                                                    }
                                                                }

                                                                default -> {
                                                                    player.sendMessage(Component.translatable("messages.claims.create.invalid-type"));
                                                                    return 0;
                                                                }
                                                            }

                                                            if (claim == null || claimId == -1) {
                                                                player.sendMessage(Component.translatable("messages.claims.remove.not-found"));
                                                                return 0;
                                                            }

                                                            OfflinePlayer target = Bukkit.getOfflinePlayer(targetPlayerName);
                                                            UUID targetUUID = target.getUniqueId();

                                                            List<UUID> whitelist = claim.getWhitelistedPlayers();

                                                            switch (action) {
                                                                case "add" -> {
                                                                    if (whitelist.contains(targetUUID)) {
                                                                        player.sendMessage(Component.translatable("messages.claims.whitelist.already"));
                                                                    } else {
                                                                        ClaimManager.addPlayerToWhitelist(claimId, targetUUID);
                                                                        player.sendMessage(Component.translatable("messages.claims.whitelist.added"));
                                                                    }
                                                                }
                                                                case "remove" -> {
                                                                    if (!whitelist.contains(targetUUID)) {
                                                                        player.sendMessage(Component.translatable("messages.claims.whitelist.player-not-found"));
                                                                    } else {
                                                                        ClaimManager.removePlayerFromWhitelist(claimId, targetUUID);
                                                                        player.sendMessage(Component.translatable("messages.claims.whitelist.removed"));
                                                                    }
                                                                }
                                                                default -> {
                                                                    player.sendMessage(Component.translatable("messages.claims.whitelist.invalid-action"));
                                                                    return 0;
                                                                }
                                                            }

                                                            return 1;

                                                        })
                                                )
                                        )
                                )
                        )
        );
        cmd.then(
                Commands.literal("protections")
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    List<String> types = List.of("player", "team");
                                    for (String type : types) {
                                        if (type.toLowerCase().startsWith(builder.getRemaining().toLowerCase())) {
                                            builder.suggest(type);
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("claim", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            var sender = ctx.getSource().getSender();
                                            if (!(sender instanceof Player player)) return builder.buildFuture();
                                            String type = StringArgumentType.getString(ctx, "type");

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
                                            }

                                            return builder.buildFuture();
                                        })
                                        .then(Commands.argument("key", ArgumentTypes.namespacedKey())
                                                .suggests((ctx, builder) -> {
                                                    String remaining = builder.getRemaining();
                                                    for (Protection flag : protectionsReg) {
                                                        String s = flag.getKey().toString();
                                                        if (!flag.isHidden() && s.startsWith(remaining.toLowerCase())) {
                                                            builder.suggest(s);
                                                        }
                                                    }
                                                    return builder.buildFuture();
                                                })
                                                .then(Commands.argument("value", StringArgumentType.word())
                                                        .suggests((ctx, builder) -> {
                                                            List<String> bools = List.of("true", "false");
                                                            for (String b : bools) {
                                                                if (b.startsWith(builder.getRemaining().toLowerCase())) {
                                                                    builder.suggest(b);
                                                                }
                                                            }
                                                            return builder.buildFuture();
                                                        })
                                                        .executes(ctx -> {
                                                            var sender = ctx.getSource().getSender();
                                                            if (!(sender instanceof Player player)) {
                                                                sender.sendMessage(Component.translatable("messages.error.not-a-player"));
                                                                return 0;
                                                            }

                                                            String type = StringArgumentType.getString(ctx, "type").toLowerCase();
                                                            String claimName = StringArgumentType.getString(ctx, "claim");
                                                            String flagName = StringArgumentType.getString(ctx, "key").toLowerCase();
                                                            String valueStr = StringArgumentType.getString(ctx, "value").toLowerCase();

                                                            boolean value;
                                                            if (valueStr.equals("true")) {
                                                                value = true;
                                                            } else if (valueStr.equals("false")) {
                                                                value = false;
                                                            } else {
                                                                player.sendMessage(Component.translatable("messages.claims.protections.invalid-value"));
                                                                return 0;
                                                            }

                                                            NamespacedKey flag;
                                                            try {
                                                                flag = NamespacedKey.fromString(flagName);

                                                                if (flag == null) {
                                                                    player.sendMessage(Component.translatable("messages.claims.protections.invalid-flag"));
                                                                    return 0;
                                                                }

                                                            } catch (IllegalArgumentException e) {
                                                                player.sendMessage(Component.translatable("messages.claims.protections.invalid-flag"));
                                                                return 0;
                                                            }

                                                            if (Protection.isHiddenProtection(protectionsReg, flag)) {
                                                                player.sendMessage(Component.translatable("messages.claims.protections.invalid-flag"));
                                                                return 0;
                                                            }

                                                            Claim claim = null;
                                                            long claimId = -1;

                                                            switch (type) {
                                                                case "player" -> {
                                                                    List<Long> ids = ClaimManager.playerOwner.getOrDefault(player.getUniqueId(), List.of());
                                                                    for (long id : ids) {
                                                                        if (claimName.equalsIgnoreCase(ClaimManager.getClaimNameById(id))) {
                                                                            claim = ClaimManager.getClaimByID(id);
                                                                            claimId = id;
                                                                            break;
                                                                        }
                                                                    }
                                                                }
                                                                case "team" -> {
                                                                    Team team = ClaimManager.getPlayerTeam(player);
                                                                    if (team == null) {
                                                                        player.sendMessage(Component.translatable("messages.error.not-in-a-team"));
                                                                        return 0;
                                                                    }
                                                                    List<Long> ids = ClaimManager.teamOwner.getOrDefault(team.getName(), List.of());
                                                                    for (long id : ids) {
                                                                        if (claimName.equalsIgnoreCase(ClaimManager.getClaimNameById(id))) {
                                                                            claim = ClaimManager.getClaimByID(id);
                                                                            claimId = id;
                                                                            break;
                                                                        }
                                                                    }
                                                                }
                                                                default -> {
                                                                    player.sendMessage(Component.translatable("messages.claims.protections.invalid-type"));
                                                                    return 0;
                                                                }
                                                            }

                                                            if (claim == null || claimId == -1) {
                                                                player.sendMessage(Component.translatable("messages.claims.remove.not-found"));
                                                                return 0;
                                                            }

                                                            if (value) {
                                                                ClaimManager.addProtection(claimId, flag);
                                                                player.sendMessage(Message.msg(player, "messages.claims.protections.added", Map.of("flag", flag.toString())));
                                                            } else {
                                                                ClaimManager.removeProtection(claimId, flag);
                                                                player.sendMessage(Message.msg(player, "messages.claims.protections.removed", Map.of("flag", flag.toString())));
                                                            }

                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                        )
        );

        return cmd.build();
    }
}
