package net.mathias2246.buildmc.claims;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.mathias2246.buildmc.util.LocationUtil;
import net.mathias2246.buildmc.util.Message;
import net.mathias2246.buildmc.util.Sounds;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static net.mathias2246.buildmc.Main.*;

@SuppressWarnings("UnstableApiUsage")
public class ClaimTool implements Listener {

    public static final @NotNull NamespacedKey CLAIM_TOOL_ITEM_PDC_KEY = Objects.requireNonNull(NamespacedKey.fromString("buildmc:is_claim_tool_item"));

    public static final @NotNull NamespacedKey REMOVE_TOOL_ITEM_PDC_KEY = Objects.requireNonNull(NamespacedKey.fromString("buildmc:is_remove_tool_item"));


    public static int selectionSizeLimit = 8;

    public static ItemStack claimToolItemstack;

    public static ItemStack removeToolItemstack;

    public static void setup() {
        var claimToolItem = Material.getMaterial(config.getString("claims.tool.tool-item", "carrot_on_a_stick").toUpperCase());
        if (claimToolItem == null) claimToolItem = Material.CARROT_ON_A_STICK;

        claimToolItemstack = new ItemStack(claimToolItem);
        removeToolItemstack = new ItemStack(claimToolItem);

        selectionSizeLimit = config.getInt("claims.tool.limit-selection", 8);
        if (selectionSizeLimit < 0) selectionSizeLimit = Integer.MAX_VALUE;

        var meta = removeToolItemstack.getItemMeta();
        if (meta != null) {
            meta.setTool(null);
            meta.addItemFlags(
                    ItemFlag.HIDE_ATTRIBUTES,
                    ItemFlag.HIDE_UNBREAKABLE
            );
            meta.setUnbreakable(true);
            meta.setRarity(ItemRarity.UNCOMMON);
            meta.setEnchantmentGlintOverride(true);
            meta.setEnchantable(null);
            meta.getPersistentDataContainer().set(REMOVE_TOOL_ITEM_PDC_KEY, PersistentDataType.BOOLEAN, true);
            removeToolItemstack.setItemMeta(meta);
        }


        ItemMeta m = claimToolItemstack.getItemMeta();
        if (m != null) {

            m.setTool(null);
            m.addItemFlags(
                    ItemFlag.HIDE_ATTRIBUTES,
                    ItemFlag.HIDE_UNBREAKABLE
            );
            m.setUnbreakable(true);
            m.setRarity(ItemRarity.UNCOMMON);
            m.setEnchantmentGlintOverride(true);
            m.setEnchantable(null);
            m.getPersistentDataContainer().set(CLAIM_TOOL_ITEM_PDC_KEY, PersistentDataType.BOOLEAN, true);
            claimToolItemstack.setItemMeta(m);
        }
    }

    /**Gives the custom claim-tool to the given player*/
    public static void giveToolToPlayer(@NotNull Player player) {
        var i = claimToolItemstack.clone();
        ItemMeta m = i.getItemMeta();

        if (m != null) {

            m.setItemName(LegacyComponentSerializer.legacySection().serialize(Message.msg(player, "messages.claims.tool.tool-name")));
            m.setLore(
                    List.of(
                            LegacyComponentSerializer.legacySection().serialize(Message.msg(player, "messages.claims.tool.tool-tooltip-line1")),
                            LegacyComponentSerializer.legacySection().serialize(Message.msg(player, "messages.claims.tool.tool-tooltip-line2")),
                            LegacyComponentSerializer.legacySection().serialize(Message.msg(player, "messages.claims.tool.tool-tooltip-line3")),
                            LegacyComponentSerializer.legacySection().serialize(Message.msg(player, "messages.claims.tool.tool-tooltip-line4"))
                    )
            );
            i.setItemMeta(m);
        }

        player.getInventory().addItem(i);
    }
    /**Gives the custom claim-tool to the given player*/
    public static void giveRemoveToolToPlayer(@NotNull Player player) {
        var i = removeToolItemstack.clone();
        ItemMeta m = i.getItemMeta();
        if (m != null) {
            m.setItemName(LegacyComponentSerializer.legacySection().serialize(Message.msg(player, "messages.claims.tool.remove-tool-name")));
            m.setLore(
                    List.of(
                            LegacyComponentSerializer.legacySection().serialize(Message.msg(player, "messages.claims.tool.remove-tool-tooltip-line1")),
                            LegacyComponentSerializer.legacySection().serialize(Message.msg(player, "messages.claims.tool.remove-tool-tooltip-line2")),
                            LegacyComponentSerializer.legacySection().serialize(Message.msg(player, "messages.claims.tool.remove-tool-tooltip-line3")),
                            LegacyComponentSerializer.legacySection().serialize(Message.msg(player, "messages.claims.tool.remove-tool-tooltip-line4"))
                    )
            );
            i.setItemMeta(m);
        }

        player.getInventory().addItem(i);
    }


    /**Checks if the given ItemStack is a claim-tool item*/
    public static boolean isClaimTool(@Nullable ItemStack item) {
        if (item == null) return false;
        var meta = item.getItemMeta();
        if (meta == null) return false;
        var pdc = meta.getPersistentDataContainer();
        return pdc.has(CLAIM_TOOL_ITEM_PDC_KEY);
    }

    /**Checks if the given ItemStack is a remove-tool item*/
    public static boolean isClaimRemoveTool(@Nullable ItemStack item) {
        if (item == null) return false;
        var meta = item.getItemMeta();
        if (meta == null) return false;
        var pdc = meta.getPersistentDataContainer();
        return pdc.has(REMOVE_TOOL_ITEM_PDC_KEY);
    }

    @EventHandler
    public void onPlayerUseHoe(PlayerInteractEvent event) {
        if (isClaimTool(event.getItem())) {

            event.setCancelled(true);
            Player player = event.getPlayer();

            Location at = null;
            if (event.getClickedBlock() != null) at = event.getClickedBlock().getLocation();
            else at = player.getLocation();


            if (!player.isSneaking()) {
                if (!ClaimManager.isNotClaimedOrOwn(ClaimManager.getPlayerTeam(player), at)) {
                    audiences.player(player).sendMessage(Message.msg(player, "messages.claims.tool.other-claim-in-selection"));
                    Sounds.playSound(player, Sounds.MISTAKE);
                    return;
                }
                Sounds.playSound(player, Sounds.SUCCESS);
                player.setMetadata(
                        "claim_tool_pos1",
                        new FixedMetadataValue(
                                plugin,
                                LocationUtil.serialize(at)
                        )
                );
                audiences.player(player).sendMessage(Message.msg(player, "messages.claims.tool.successfully-set-pos"));
            } else {
                // Fail if the player has no first position set
                if (!player.hasMetadata("claim_tool_pos1")) {
                    audiences.player(player).sendActionBar(Message.msg(player, "messages.claims.tool.missing-first-pos"));
                    Sounds.playSound(player, Sounds.MISTAKE);
                    return;
                }

                var team = ClaimManager.getPlayerTeam(player);
                // Fail if the player is in no team
                if (team == null) {
                    audiences.player(player).sendMessage(
                            Message.msg(player, "messages.claims.tool.no-team-to-select")
                    );
                    Sounds.playSound(player, Sounds.MISTAKE);
                    return;
                }

                tryClaimArea(
                        player,
                        team,
                        LocationUtil.deserialize(player.getMetadata("claim_tool_pos1").getFirst().asString()),
                        at
                );
            }
        } else if (isClaimRemoveTool(event.getItem())) {
            event.setCancelled(true);
            Player player = event.getPlayer();

            Location at = null;
            if (event.getClickedBlock() != null) at = event.getClickedBlock().getLocation();
            else at = player.getLocation();

            if (!player.isSneaking()) {
                if (!ClaimManager.isNotClaimedOrOwn(ClaimManager.getPlayerTeam(player), at)) {
                    audiences.player(player).sendMessage(Message.msg(player, "messages.claims.tool.other-claim-in-selection"));
                    Sounds.playSound(player, Sounds.MISTAKE);
                    return;
                }
                Sounds.playSound(player, Sounds.SUCCESS);
                player.setMetadata(
                        "claim_tool_pos1",
                        new FixedMetadataValue(
                                plugin,
                                LocationUtil.serialize(at)
                        )
                );
                audiences.player(player).sendMessage(Message.msg(player, "messages.claims.tool.successfully-set-pos"));
            } else {
                // Fail if the player has no first position set
                if (!player.hasMetadata("claim_tool_pos1")) {
                    audiences.player(player).sendActionBar(Message.msg(player, "messages.claims.tool.missing-first-pos"));
                    Sounds.playSound(player, Sounds.MISTAKE);
                    return;
                }

                var team = ClaimManager.getPlayerTeam(player);
                // Fail if the player is in no team
                if (team == null) {
                    audiences.player(player).sendMessage(
                            Message.msg(player, "messages.claims.tool.no-team-to-select")
                    );
                    Sounds.playSound(player, Sounds.MISTAKE);
                    return;
                }

                tryRemoveArea(
                        player,
                        team,
                        LocationUtil.deserialize(player.getMetadata("claim_tool_pos1").getFirst().asString()),
                        at
                );
            }
        }
    }

    private static void tryRemoveArea(@NotNull Player player, @NotNull Team team, @NotNull Location from, @NotNull Location to) {
        int sx = Math.min(from.getChunk().getX(), to.getChunk().getX());
        int sz = Math.min(from.getChunk().getZ(), to.getChunk().getZ());
        int ex = Math.max(from.getChunk().getX(), to.getChunk().getX());
        int ez = Math.max(from.getChunk().getZ(), to.getChunk().getZ());


        World world = from.getWorld();
        if (world == null) return;

        if (
                ex-sx > selectionSizeLimit ||
                        ez-sz > selectionSizeLimit
        ) {
            var m = Message.msg(player, "messages.claims.tool.selection-too-large");

            var r = TextReplacementConfig.builder().matchLiteral("%selection_limit%").replacement(Integer.toString(selectionSizeLimit));
            audiences.player(player).sendMessage(m.replaceText(r.build()));
            Sounds.playSound(player, Sounds.MISTAKE);
            return;
        }

        int count = 0;
        for (int z = sz; z <= ez; z++) {
            for (int x = sx; x <= ex; x++) {
                Chunk chunk = world.getChunkAt(x, z);
                // If someone else's claim is in this selection, fail
                if (!ClaimManager.isNotClaimedOrOwn(team, chunk)) {
                    audiences.player(player).sendMessage(Message.msg(player, "messages.claims.tool.other-claim-in-selection"));
                    Sounds.playSound(player, Sounds.MISTAKE);
                    return;
                } else if (ClaimManager.hasOwner(chunk)) {

                    count++;

                }
            }
        }

        ClaimManager.forceClaimArea(null, world, sx, sz, ex, ez);

        ClaimDataInstance cm = claimManager.getEntryOrNew(team);
        if (cm.chunksLeft >= 0) cm.chunksLeft += count;

        player.removeMetadata("claim_tool_pos1", plugin);

        Sounds.playSound(player, Sounds.SUCCESS);
        audiences.player(player).sendMessage(
                buildRemoveSuccessMessage(player, team)
        );
    }

    private static void tryClaimArea(@NotNull Player player, @NotNull Team team, @NotNull Location from, @NotNull Location to) {
        int sx = Math.min(from.getChunk().getX(), to.getChunk().getX());
        int sz = Math.min(from.getChunk().getZ(), to.getChunk().getZ());
        int ex = Math.max(from.getChunk().getX(), to.getChunk().getX());
        int ez = Math.max(from.getChunk().getZ(), to.getChunk().getZ());

        World world = from.getWorld();
        if (world == null) return;

        int count = 0;
        int chunksLeft = ClaimManager.getChunksLeft(claimManager, team);

        for (int z = sz; z <= ez; z++) {
            for (int x = sx; x <= ex; x++) {
                Chunk chunk = world.getChunkAt(x, z);
                // If someone else's claim is in this selection, fail
                if (!ClaimManager.isNotClaimedOrOwn(team, chunk)) {
                    audiences.player(player).sendMessage(Message.msg(player, "messages.claims.tool.other-claim-in-selection"));
                    Sounds.playSound(player, Sounds.MISTAKE);
                    return;
                } else if (!ClaimManager.hasOwner(chunk)) {
                    if (chunksLeft < 0) continue;

                    count++;
                    if (count > chunksLeft) { // If your team has no chunks left to claim, fail
                        audiences.player(player).sendMessage(Message.msg(player, "messages.claims.tool.no-chunks-left"));
                        Sounds.playSound(player, Sounds.MISTAKE);
                        return;
                    }

                }
            }
        }

        ClaimManager.forceClaimArea(team, world, sx, sz, ex, ez);

        if (chunksLeft > 0) claimManager.getEntryOrNew(team).chunksLeft -= count;

        player.removeMetadata("claim_tool_pos1", plugin);

        Sounds.playSound(player, Sounds.SUCCESS);
        audiences.player(player).sendMessage(
            buildSuccessMessage(player, team)
        );
    }


    // Replaces the %chunks_left% placeholder with the number of chunks left
    private static Component buildSuccessMessage(@NotNull Player player, @NotNull Team team) {
        int i = ClaimManager.getChunksLeft(claimManager, team);
        if (i < 0) {
            return Component.translatable("messages.claims.tool.successfully-claimed-area-no-count");
        }

        var r = TextReplacementConfig.builder().matchLiteral("%chunks_left%").replacement(Integer.toString(i));

        return Message.msg(player, "messages.claims.tool.successfully-claimed-area").replaceText(r.build());
    }

    // Replaces the %chunks_left% placeholder with the number of chunks left
    private static Component buildRemoveSuccessMessage(@NotNull Player player, @NotNull Team team) {
        int i = ClaimManager.getChunksLeft(claimManager, team);
        if (i < 0) {
            return Component.translatable("messages.claims.tool.successfully-removed-area-no-count");
        }

        var r = TextReplacementConfig.builder().matchLiteral("%chunks_left%").replacement(Integer.toString(i));

        return Message.msg(player, "messages.claims.tool.successfully-removed-area").replaceText(r.build());
    }
}
