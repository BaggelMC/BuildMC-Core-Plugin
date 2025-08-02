package net.mathias2246.buildmc.claims;


import com.destroystokyo.paper.profile.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
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

    public static ItemStack claimToolItemstack;

    public static void setup() {
        var claimToolItem = Material.getMaterial(config.getString("claims.tool.tool-item", "carrot_on_a_stick").toUpperCase());
        if (claimToolItem == null) claimToolItem = Material.CARROT_ON_A_STICK;

        claimToolItemstack = new ItemStack(claimToolItem);
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

        m.itemName(Message.msg(player, "messages.claims.tool.tool-name"));
        m.lore(
                List.of(
                        Message.msg(player, "messages.claims.tool.tool-tooltip-line1"),
                        Message.msg(player, "messages.claims.tool.tool-tooltip-line2"),
                        Message.msg(player, "messages.claims.tool.tool-tooltip-line3"),
                        Message.msg(player, "messages.claims.tool.tool-tooltip-line4")
                )
        );
        i.setItemMeta(m);

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

    @EventHandler
    public void onPlayerUseHoe(PlayerInteractEvent event) {
        if (!isClaimTool(event.getItem())) return;

        event.setCancelled(true);
        Player player = event.getPlayer();

        if (!player.isSneaking()) {
            if (!ClaimManager.isNotClaimedOrOwn(ClaimManager.getPlayerTeam(player), player.getLocation())) {
                player.sendMessage(Message.msg(player, "messages.claims.tool.other-claim-in-selection"));
                Sounds.playSound(player, Sounds.MISTAKE);
                return;
            }
            Sounds.playSound(player, Sounds.SUCCESS);
            player.setMetadata(
                    "claim_tool_pos1",
                    new FixedMetadataValue(
                            plugin,
                            LocationUtil.serialize(player.getLocation())
                    )
            );
            player.sendMessage(Message.msg(player, "messages.claims.tool.successfully-set-pos"));
        } else {
            // Fail if the player has no first position set
            if (!player.hasMetadata("claim_tool_pos1")) {
                player.sendActionBar(Message.msg(player, "messages.claims.tool.missing-first-pos"));
                Sounds.playSound(player, Sounds.MISTAKE);
                return;
            }

            var team = ClaimManager.getPlayerTeam(player);
            // Fail if the player is in no team
            if (team == null) {
                player.sendMessage(
                        Message.msg(player, "messages.claims.tool.no-team-to-select")
                );
                Sounds.playSound(player, Sounds.MISTAKE);
                return;
            }

            tryClaimArea(
                    player,
                    team,
                    LocationUtil.deserialize(player.getMetadata("claim_tool_pos1").getFirst().asString()),
                    player.getLocation()
            );
        }
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
                    player.sendMessage(Message.msg(player, "messages.claims.tool.other-claim-in-selection"));
                    Sounds.playSound(player, Sounds.MISTAKE);
                    return;
                } else if (!ClaimManager.hasOwner(chunk)) {
                    if (chunksLeft < 0) continue;

                    count++;
                    if (count > chunksLeft) { // If your team has no chunks left to claim, fail
                        player.sendMessage(Message.msg(player, "messages.claims.tool.no-chunks-left"));
                        Sounds.playSound(player, Sounds.MISTAKE);
                        return;
                    }

                }
            }
        }

        ClaimManager.forceClaimArea(team, world, sx, sz, ex, ez);

        if (chunksLeft > 0) claimManager.claims.get(team).chunksLeft -= count;

        player.removeMetadata("claim_tool_pos1", plugin);

        Sounds.playSound(player, Sounds.SUCCESS);
        player.sendMessage(
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
}
