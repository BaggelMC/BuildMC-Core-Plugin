package net.mathias2246.buildmc.claims;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.mathias2246.buildmc.util.LocationUtil;
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

    public static final Material CLAIM_TOOL_ITEM = Material.WOODEN_HOE;

    public static final ItemStack CLAIM_TOOL_ITEMSTACK;

    public static Component missing_first_selection;

    public static Component selected_position;

    public static Component other_claim_in_selection;

    public static Component successfully_claimed_area;

    public static void setup() {
        missing_first_selection = JSONComponentSerializer.json().deserialize(config.getString(
                "claims.tool.missing-first-pos",
                "{\"text\": \"You need to select the other corner first!\", \"color\": \"red\"}"
        ));
        selected_position = JSONComponentSerializer.json().deserialize(config.getString(
                "claims.tool.successfully-set-pos",
                "{\"text\": \"Successfully set position!\", \"color\": \"green\"}"
        ));
        other_claim_in_selection = JSONComponentSerializer.json().deserialize(config.getString(
                "claims.tool.other-claim-in-selection",
                "{\"text\": \"There is someone else's claim in your selection!.\", \"color\": \"red\"}"
        ));
        successfully_claimed_area = JSONComponentSerializer.json().deserialize(config.getString(
                "claims.tool.successfully-claimed-area",
                "{\"text\":\"Successfully claimed area!\", \"color\":\"green\"}"
        ));
    }

    static {
        CLAIM_TOOL_ITEMSTACK = new ItemStack(CLAIM_TOOL_ITEM);
        ItemMeta m = CLAIM_TOOL_ITEMSTACK.getItemMeta();
        if (m != null) {

            m.setTool(null);
            m.setItemName("Select Claim Corners");
            m.setLore(
                    List.of(
                            "Use this tool to set the corners of your teams claim.",
                            "After setting the first corner by clicking with the tool select the second corner by sneak clicking.",
                            "When selecting on of your existing claims it is removed."
                    )
            );
            m.addItemFlags(
                    ItemFlag.HIDE_ATTRIBUTES,
                    ItemFlag.HIDE_UNBREAKABLE
            );
            m.setUnbreakable(true);
            m.setRarity(ItemRarity.UNCOMMON);
            m.setEnchantmentGlintOverride(true);
            m.setEnchantable(null);
            m.getPersistentDataContainer().set(CLAIM_TOOL_ITEM_PDC_KEY, PersistentDataType.BOOLEAN, true);
            CLAIM_TOOL_ITEMSTACK.setItemMeta(m);
        }
    }

    /**Gives the custom claim-tool to the given player*/
    public static void giveToolToPlayer(@NotNull Player player) {
        player.getInventory().addItem(CLAIM_TOOL_ITEMSTACK);
    }

    /**Checks if the given ItemStack is a claim-tool item*/
    public static boolean isClaimTool(@Nullable ItemStack item) {
        if (item == null) return false;
        var meta = item.getItemMeta();
        if (meta == null) return false;
        var pdc = meta.getPersistentDataContainer();
        if (!pdc.has(CLAIM_TOOL_ITEM_PDC_KEY)) return false;
        return Boolean.TRUE.equals(pdc.get(CLAIM_TOOL_ITEM_PDC_KEY, PersistentDataType.BOOLEAN));
    }

    public static final @NotNull NamespacedKey PLAYER_META_CLAIM_POS1 = Objects.requireNonNull(NamespacedKey.fromString("buildmc:claim_tool_pos1"));
    public static final @NotNull NamespacedKey PLAYER_META_CLAIM_POS2 = Objects.requireNonNull(NamespacedKey.fromString("buildmc:claim_tool_pos2"));

    @EventHandler
    public void onPlayerUseHoe(PlayerInteractEvent event) {
        if (!isClaimTool(event.getItem())) return;

        event.setCancelled(true);
        Player player = event.getPlayer();

        if (!player.isSneaking()) {
            Sounds.playSound(player, Sounds.SUCCESS);
            player.setMetadata(
                    "claim_tool_pos1",
                    new FixedMetadataValue(
                            plugin,
                            LocationUtil.serialize(player.getLocation())
                    )
            );
            audiences.player(player).sendMessage(selected_position);
        } else {
            if (!player.hasMetadata("claim_tool_pos1")) {
                audiences.player(player).sendActionBar(missing_first_selection);
                Sounds.playSound(player, Sounds.MISTAKE);
                return;
            }
            var team = ClaimManager.getPlayerTeam(player);
            if (team == null) {
                audiences.player(player).sendMessage(
                        Component.text("You need to be in a team to set a claim!").color(TextColor.color(255, 85, 85))
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

        for (int z = sz; z < ez; z++) {
            for (int x = sx; x < ex; x++) {
                Chunk chunk = world.getChunkAt(x, z);
                if (!ClaimManager.isNotClaimedOrOwn(team, chunk)) {
                    audiences.player(player).sendMessage(other_claim_in_selection);
                    Sounds.playSound(player, Sounds.MISTAKE);
                    return;
                }
            }
        }

        ClaimManager.forceClaimArea(team, world, sx, sz, ex, ez);

        Sounds.playSound(player, Sounds.SUCCESS);
        audiences.player(player).sendMessage(
            buildSuccessMessage(team)
        );
    }



    private static Component buildSuccessMessage(@NotNull Team team) {
        int i = ClaimManager.getChunksLeft(team);
        Component c = successfully_claimed_area.asComponent();
        var r = TextReplacementConfig.builder().matchLiteral("%chunks_left%").replacement(Integer.toString(i));

        return c.replaceText(r.build());
    }
}
