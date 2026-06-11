package net.mathias2246.buildmc.commands.claim;

import com.google.common.collect.ImmutableSet;
import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.claims.ClaimType;
import net.mathias2246.buildmc.claims.ClaimLogger;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.claims.tools.ClaimSelectionTool;
import net.mathias2246.buildmc.util.AudienceUtil;
import net.mathias2246.buildmc.util.LocationUtil;
import net.mathias2246.buildmc.util.Message;
import net.mathias2246.buildmc.util.SoundUtil;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;

import static net.mathias2246.buildmc.CoreMain.plugin;

public class ClaimCreate {

    /**
     * Validates that the player's current area selection is eligible to be claimed.
     * Checks selection existence, world consistency, size limits, world allowlist,
     * and overlap with existing claims.
     *
     * <p>Sends feedback messages and plays sounds directly on the player for any
     * failed check, so callers do not need to handle error messaging themselves.
     *
     * @param player The player attempting to create a claim
     * @return {@code true} if all area checks pass and the selection can be claimed;
     *         {@code false} if any check fails
     */
    public static boolean validateClaimArea(@NotNull Player player) {
        if (!player.getPersistentDataContainer().has(Objects.requireNonNull(NamespacedKey.fromString("buildmc:claim_tool_first_selection")))
                || !player.getPersistentDataContainer().has(Objects.requireNonNull(NamespacedKey.fromString("buildmc:claim_tool_second_selection")))) {
            AudienceUtil.sendMessage(player, Component.translatable("messages.claims.create.missing-positions"));
            return false;
        }

        Location pos1 = getFirstSelection(player);
        Location pos2 = getSecondSelection(player);

        if (pos1 == null || pos2 == null) {
            AudienceUtil.sendMessage(player, Component.translatable("messages.claims.create.missing-positions"));
            CoreMain.soundManager.playSound(player, SoundUtil.notification);
            return false;
        }

        if (!Objects.equals(pos1.getWorld(), pos2.getWorld())) {
            AudienceUtil.sendMessage(player, Component.translatable("messages.claims.create.different-worlds"));
            CoreMain.soundManager.playSound(player, SoundUtil.notification);
            return false;
        }

        if (ClaimSelectionTool.isSelectionToLarge(pos1, pos2, player)) {
            AudienceUtil.sendMessage(player, Component.translatable("messages.claims.tool.selection-too-large"));
            CoreMain.soundManager.playSound(player, SoundUtil.notification);
            return false;
        }

        if (!player.hasPermission("buildmc.bypass-claim-dimension-list")) {
            if (!ClaimManager.isWorldAllowed(Objects.requireNonNull(pos1.getWorld()))) {
                AudienceUtil.sendMessage(player, Component.translatable("messages.claims.create.world-not-allowed"));
                CoreMain.soundManager.playSound(player, SoundUtil.notification);
                return false;
            }
        }

        return checkNoOverlap(player, pos1, pos2);
    }

    /**
     * Checks whether the given area overlaps any existing claims, and sends the
     * appropriate error message and sound to the player if it does.
     *
     * @param player The player attempting to create a claim
     * @param pos1   First corner of the selection
     * @param pos2   Second corner of the selection
     * @return {@code true} if the area is clear; {@code false} if it overlaps an existing claim
     */
    private static boolean checkNoOverlap(@NotNull Player player, @NotNull Location pos1, @NotNull Location pos2) {
        ImmutableSet<Claim> overlappingClaims;
        try {
            overlappingClaims = ClaimManager.getClaimsInArea(pos1, pos2);
        } catch (SQLException e) {
            plugin.getLogger().severe("There was an error while getting the claims in an area: " + e.getMessage());
            CoreMain.soundManager.playSound(player, SoundUtil.mistake);
            AudienceUtil.sendMessage(player, Component.translatable("messages.claims.create.error-database"));
            return false;
        }

        if (!overlappingClaims.isEmpty()) {
            boolean serverProtected = overlappingClaims.stream()
                    .anyMatch(claim -> claim.getType() == ClaimType.SERVER || claim.getType() == ClaimType.PLACEHOLDER);

            if (serverProtected) {
                AudienceUtil.sendMessage(player, Component.translatable("messages.claims.create.protected-server"));
            } else {
                AudienceUtil.sendMessage(player, Component.translatable("messages.claims.create.overlap"));
            }
            CoreMain.soundManager.playSound(player, SoundUtil.notification);
            return false;
        }

        return true;
    }

    /**
     * Attempts to create a claim for the player with the given type and name.
     *
     * @param player           The player creating the claim
     * @param type             The claim type ("player", "team", "server", "placeholder")
     * @param name             The name for the new claim
     * @param checksAlreadyRan Pass {@code true} if {@link #validateClaimArea(Player)} has
     *                         already been called and passed for this player's current
     *                         selection. Pass {@code false} to have the area checks run
     *                         here before proceeding.
     * @return 1 on success, 0 on failure
     */
    public static int createClaimCommand(@NotNull Player player, String type, String name, boolean checksAlreadyRan) {
        if (!checksAlreadyRan && !validateClaimArea(player)) {
            return 0;
        }

        Location pos1 = getFirstSelection(player);
        Location pos2 = getSecondSelection(player);

        if (pos1 == null || pos2 == null) {
            AudienceUtil.sendMessage(player, Component.translatable("messages.claims.create.missing-positions"));
            CoreMain.soundManager.playSound(player, SoundUtil.notification);
            return 0;
        }

        // Always re-check overlap regardless of checksAlreadyRan, to close the race
        // window where another player claims the same area while the dialog is open
        if (!checkNoOverlap(player, pos1, pos2)) {
            return 0;
        }

        int newClaimChunks = LocationUtil.calculateChunkArea(pos1, pos2);

        switch (type.toLowerCase()) {
            case "player" -> {
                int maxChunksAllowed = plugin.getConfig().getInt("claims.player-max-chunk-claim-amount");
                int remainingChunks = ClaimManager.playerRemainingClaims.getOrDefault(player.getUniqueId().toString(), maxChunksAllowed);
                if ((remainingChunks - newClaimChunks) < 0) {
                    AudienceUtil.sendMessage(player, Message.msg(player, "messages.claims.create.no-remaining-claims", Map.of("no-remaining-claims", String.valueOf(remainingChunks))));
                    CoreMain.soundManager.playSound(player, SoundUtil.mistake);
                    return 0;
                }

                try {
                    if (ClaimManager.doesOwnerHaveClaimWithName(player.getUniqueId().toString(), name)) {
                        AudienceUtil.sendMessage(player, Component.translatable("messages.claims.create.duplicate-name"));
                        CoreMain.soundManager.playSound(player, SoundUtil.mistake);
                        return 1;
                    }
                } catch (SQLException e) {
                    plugin.getLogger().severe("SQL Error while trying to check claim name availability: " + e);
                    AudienceUtil.sendMessage(player, Component.translatable("messages.claims.create.error-database"));
                    CoreMain.soundManager.playSound(player, SoundUtil.mistake);
                    return 0;
                }

                boolean success = ClaimManager.tryClaimPlayerArea(player, name, pos1, pos2) != null;
                if (success) {
                    AudienceUtil.sendMessage(player, Message.msg(player, "messages.claims.create.success", Map.of("remaining_claims", String.valueOf(remainingChunks - newClaimChunks))));
                    CoreMain.soundManager.playSound(player, SoundUtil.success);
                    removeSelectionData(player);
                    ClaimLogger.logClaimCreated(player, name);
                    return 1;
                } else {
                    AudienceUtil.sendMessage(player, Component.translatable("messages.claims.create.failed"));
                    CoreMain.soundManager.playSound(player, SoundUtil.mistake);
                    return 0;
                }
            }

            case "team" -> {
                Team team = ClaimManager.getPlayerTeam(player);
                if (team == null) {
                    AudienceUtil.sendMessage(player, Component.translatable("messages.error.not-in-a-team"));
                    CoreMain.soundManager.playSound(player, SoundUtil.mistake);
                    return 0;
                }

                int maxChunksAllowed = plugin.getConfig().getInt("claims.team-max-chunk-claim-amount");
                int remainingChunks = ClaimManager.teamRemainingClaims.getOrDefault(team.getName(), maxChunksAllowed);
                if ((remainingChunks - newClaimChunks) < 0) {
                    AudienceUtil.sendMessage(player, Message.msg(player, "messages.claims.create.no-remaining-claims", Map.of("remaining_claims", String.valueOf(remainingChunks))));
                    CoreMain.soundManager.playSound(player, SoundUtil.mistake);
                    return 0;
                }

                try {
                    if (ClaimManager.doesOwnerHaveClaimWithName(team.getName(), name)) {
                        AudienceUtil.sendMessage(player, Component.translatable("messages.claims.create.duplicate-name"));
                        CoreMain.soundManager.playSound(player, SoundUtil.mistake);
                        return 1;
                    }
                } catch (SQLException e) {
                    plugin.getLogger().severe("SQL Error while trying to check claim name availability: " + e);
                    CoreMain.soundManager.playSound(player, SoundUtil.mistake);
                    AudienceUtil.sendMessage(player, Component.translatable("messages.claims.create.error-database"));
                    return 0;
                }

                boolean success = ClaimManager.tryClaimTeamArea(team, name, pos1, pos2) != null;
                if (success) {
                    AudienceUtil.sendMessage(player, Message.msg(player, "messages.claims.create.success", Map.of("remaining_claims", String.valueOf(remainingChunks - newClaimChunks))));
                    removeSelectionData(player);
                    CoreMain.soundManager.playSound(player, SoundUtil.success);
                    ClaimLogger.logClaimCreated(player, name);
                    return 1;
                } else {
                    AudienceUtil.sendMessage(player, Component.translatable("messages.claims.create.failed"));
                    CoreMain.soundManager.playSound(player, SoundUtil.mistake);
                    return 0;
                }
            }

            case "server", "placeholder" -> {
                if (!player.hasPermission("buildmc.admin")) {
                    AudienceUtil.sendMessage(player, Component.translatable("messages.error.no-permission"));
                    CoreMain.soundManager.playSound(player, SoundUtil.mistake);
                    return 0;
                }

                try {
                    if (ClaimManager.doesOwnerHaveClaimWithName(type.toLowerCase(), name)) {
                        AudienceUtil.sendMessage(player, Component.translatable("messages.claims.create.duplicate-name"));
                        CoreMain.soundManager.playSound(player, SoundUtil.mistake);
                        return 1;
                    }
                } catch (SQLException e) {
                    plugin.getLogger().severe("SQL Error while trying to check claim name availability: " + e);
                    AudienceUtil.sendMessage(player, Component.translatable("messages.claims.create.error-database"));
                    CoreMain.soundManager.playSound(player, SoundUtil.mistake);
                    return 0;
                }

                boolean success = switch (type.toLowerCase()) {
                    case "server" -> ClaimManager.tryClaimServerArea(name, pos1, pos2) != null;
                    case "placeholder" -> ClaimManager.tryClaimPlaceholderArea(name, pos1, pos2) != null;
                    default -> false;
                };

                if (success) {
                    AudienceUtil.sendMessage(player, Message.msg(player,
                            "messages.claims.create.success-" + type.toLowerCase(),
                            Map.of("claim_name", name)
                    ));
                    CoreMain.soundManager.playSound(player, SoundUtil.success);
                    removeSelectionData(player);
                    ClaimLogger.logClaimCreated(player, name);
                    return 1;
                } else {
                    AudienceUtil.sendMessage(player, Component.translatable("messages.claims.create.failed"));
                    CoreMain.soundManager.playSound(player, SoundUtil.mistake);
                    return 0;
                }
            }

            default -> {
                AudienceUtil.sendMessage(player, Component.translatable("messages.claims.create.invalid-type"));
                CoreMain.soundManager.playSound(player, SoundUtil.mistake);
                return 0;
            }
        }
    }

    /**Tries reading the first selection position from the player's metadata.
     *
     * @param player The player that made the selection
     * @return The Location of the first selection or null if not found or invalid */
    private static @Nullable Location getFirstSelection(@NotNull Player player) {
        var l = player.getPersistentDataContainer().get(Objects.requireNonNull(NamespacedKey.fromString("buildmc:claim_tool_first_selection")), PersistentDataType.STRING);
        if (l == null || l.isEmpty()) return null;
        return LocationUtil.tryDeserialize(l);
    }

    /**Tries reading the second selection position from the player's metadata.
     *
     * @param player The player that made the selection
     * @return The Location of the second selection or null if not found or invalid */
    private static @Nullable Location getSecondSelection(@NotNull Player player) {
        var l = player.getPersistentDataContainer().get(Objects.requireNonNull(NamespacedKey.fromString("buildmc:claim_tool_second_selection")), PersistentDataType.STRING);
        if (l == null || l.isEmpty()) return null;
        return LocationUtil.tryDeserialize(l);
    }

    private static void removeSelectionData(Player player) {
        player.getPersistentDataContainer().remove(Objects.requireNonNull(NamespacedKey.fromString("buildmc:claim_tool_first_selection")));
        player.getPersistentDataContainer().remove(Objects.requireNonNull(NamespacedKey.fromString("buildmc:claim_tool_second_selection")));
    }
}