package net.mathias2246.buildmc.claims;


import com.destroystokyo.paper.ParticleBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.mathias2246.buildmc.util.LocationUtil;
import net.mathias2246.buildmc.util.Message;
import net.mathias2246.buildmc.util.Sounds;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
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

    private static boolean useRightClick = false;

    public static void setup() {
        var claimToolItem = Material.getMaterial(config.getString("claims.tool.tool-item", "carrot_on_a_stick").toUpperCase());
        if (claimToolItem == null) claimToolItem = Material.CARROT_ON_A_STICK;

        useRightClick = config.getBoolean("claims.tool.use-right-instead-of-sneak-click", false);

        claimToolItemstack = new ItemStack(claimToolItem);
        removeToolItemstack = new ItemStack(claimToolItem);

        selectionSizeLimit = config.getInt("claims.tool.limit-selection", 8);
        if (selectionSizeLimit < 0) selectionSizeLimit = Integer.MAX_VALUE;

        removeToolItemstack.editMeta(
                (meta) -> {
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
                }
        );

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

    /**Gives the custom claim-tool to the given player*/
    public static void giveRemoveToolToPlayer(@NotNull Player player) {
        var i = removeToolItemstack.clone();
        ItemMeta m = i.getItemMeta();

        m.itemName(Message.msg(player, "messages.claims.tool.remove-tool-name"));
        m.lore(
                List.of(
                        Message.msg(player, "messages.claims.tool.remove-tool-tooltip-line1"),
                        Message.msg(player, "messages.claims.tool.remove-tool-tooltip-line2"),
                        Message.msg(player, "messages.claims.tool.remove-tool-tooltip-line3"),
                        Message.msg(player, "messages.claims.tool.remove-tool-tooltip-line4")
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

    /**Checks if the given ItemStack is a remove-tool item*/
    public static boolean isClaimRemoveTool(@Nullable ItemStack item) {
        if (item == null) return false;
        var meta = item.getItemMeta();
        if (meta == null) return false;
        var pdc = meta.getPersistentDataContainer();
        return pdc.has(REMOVE_TOOL_ITEM_PDC_KEY);
    }

    public static class ParticleSpawner extends BukkitRunnable {

        public final @NotNull Player source;

        private int repeat = 32;

        public ParticleSpawner(@NotNull Player source, boolean isRemoveSelection) {
            this.source = source;

            var l = LocationUtil.deserialize(source.getMetadata("claim_tool_pos1").getFirst().asString());

            Chunk c = l.getChunk();

            l.setX((c.getX() << 4) + 8);
            l.setZ((c.getZ() << 4) + 8);
            l.setY(l.getY()+1.5);



            if (isRemoveSelection) {
                chunkPlane = new ParticleBuilder
                        (Particle.DUST)
                        .color(210, 10, 10)
                        .count(100)
                        .receivers(source)
                        .location(
                                l
                        )
                        .offset(2.6, 0, 2.6);
            }
            else chunkPlane = new ParticleBuilder
                    (Particle.DUST)
                    .color(10, 230, 10)
                    .count(100)
                    .receivers(source)
                    .location(
                            l
                    )
                    .offset(2.6, 0, 2.6);

            var cornerBase = new ParticleBuilder(Particle.DUST)
                    .color(10, 10, 230)
                    .count(30)
                    .receivers(source)
                    .offset(0,1.5d, 0);

            var l1 = new Location(
                    source.getWorld(),
                    (source.getChunk().getX()  << 4) + 0.5d,
                    l.getY(),
                    (source.getChunk().getZ()  << 4) + 0.5d
            );

            corner1 = cornerBase.clone().location(
                    l1
            );

            var l2 = new Location(
                    source.getWorld(),
                    (source.getChunk().getX()  << 4) + 15.5d,
                    l.getY(),
                    (source.getChunk().getZ()  << 4) + 0.0d
            );

            corner2 = cornerBase.clone().location(
                    l2
            );

            var l3 = new Location(
                    source.getWorld(),
                    (source.getChunk().getX()  << 4) + 0.0d,
                    l.getY(),
                    (source.getChunk().getZ()  << 4) + 15.5d
            );

            corner3 = cornerBase.clone().location(
                    l3
            );

            var l4 = new Location(
                    source.getWorld(),
                    (source.getChunk().getX()  << 4) + 15.5d,
                    l.getY(),
                    (source.getChunk().getZ()  << 4) + 15.5d
            );


            corner4 = cornerBase.clone().location(
                    l4
            );
        }

        public final ParticleBuilder chunkPlane;
        public final ParticleBuilder corner1;
        public final ParticleBuilder corner2;
        public final ParticleBuilder corner3;
        public final ParticleBuilder corner4;

        @Override
        public void run() {

            if (repeat < 0 || !source.hasMetadata("selection_particles")) {
                source.removeMetadata("selection_particles", plugin);
                this.cancel();
            }

            repeat--;

            chunkPlane.spawn();
            corner1.spawn();
            corner2.spawn();
            corner3.spawn();
            corner4.spawn();
        }
    }

    @EventHandler
    public void onPlayerUseHoe(PlayerInteractEvent event) {
        if (isClaimTool(event.getItem())) {

            event.setCancelled(true);
            Player player = event.getPlayer();

            boolean leftClick = event.getAction().equals(Action.LEFT_CLICK_BLOCK) || event.getAction().equals(Action.LEFT_CLICK_AIR);

            boolean firstSelection;
            if (useRightClick) {
                firstSelection = leftClick;
            } else firstSelection = !player.isSneaking();

            boolean removeBypass = player.hasPermission("buildmc.force-claim");

            Location at;
            if (event.getClickedBlock() != null) at = event.getClickedBlock().getLocation();
            else at = player.getLocation();

            if (player.hasCooldown(event.getItem())) {
                player.sendMessage(Component.translatable("messages.claims.tool.tool-cooldown"));
                return;
            }

            if (firstSelection) {
                if (!removeBypass && !ClaimManager.isNotClaimedOrOwn(ClaimManager.getPlayerTeam(player), at)) {
                    player.sendMessage(Message.msg(player, "messages.claims.tool.other-claim-in-selection"));
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
                player.sendMessage(Message.msg(player, "messages.claims.tool.successfully-set-pos"));

                if (!player.hasMetadata("selection_particles")) {
                    player.setMetadata("selection_particles", new FixedMetadataValue(plugin, null));
                    new ParticleSpawner(player, false).runTaskTimer(plugin, 0, 5);
                }
                player.setCooldown(event.getItem(), 60);
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
                        at,
                        removeBypass
                );
            }
        } else if (isClaimRemoveTool(event.getItem())) {
            event.setCancelled(true);
            Player player = event.getPlayer();

            boolean leftClick = event.getAction().equals(Action.LEFT_CLICK_BLOCK) || event.getAction().equals(Action.LEFT_CLICK_AIR);

            boolean firstSelection;
            if (useRightClick) {
                firstSelection = leftClick;
            } else firstSelection = !player.isSneaking();

            boolean removeBypass = player.hasPermission("buildmc.force-claim");

            Location at;
            if (event.getClickedBlock() != null) at = event.getClickedBlock().getLocation();
            else at = player.getLocation();

            if (player.hasCooldown(event.getItem())) {
                player.sendMessage(Component.translatable("messages.claims.tool.tool-cooldown"));
                return;
            }

            if (firstSelection) {
                if (!removeBypass && !ClaimManager.isNotClaimedOrOwn(ClaimManager.getPlayerTeam(player), at)) {
                    player.sendMessage(Message.msg(player, "messages.claims.tool.other-claim-in-selection"));
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
                player.sendMessage(Message.msg(player, "messages.claims.tool.successfully-set-pos"));
                if (!player.hasMetadata("selection_particles")) {
                    player.setMetadata("selection_particles", new FixedMetadataValue(plugin, null));
                    new ParticleSpawner(player, true).runTaskTimer(plugin, 0, 5);
                }
                player.setCooldown(event.getItem(), 60);
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

                tryRemoveArea(
                        player,
                        team,
                        LocationUtil.deserialize(player.getMetadata("claim_tool_pos1").getFirst().asString()),
                        at,
                        removeBypass
                );
            }
        }
    }

    private static void tryRemoveArea(@NotNull Player player, @NotNull Team team, @NotNull Location from, @NotNull Location to, boolean force) {
        int sx = Math.min(from.getChunk().getX(), to.getChunk().getX());
        int sz = Math.min(from.getChunk().getZ(), to.getChunk().getZ());
        int ex = Math.max(from.getChunk().getX(), to.getChunk().getX());
        int ez = Math.max(from.getChunk().getZ(), to.getChunk().getZ());


        World world = from.getWorld();
        if (world == null) return;

        if (
                ex-sx >= selectionSizeLimit ||
                        ez-sz >= selectionSizeLimit
        ) {
            var m = Message.msg(player, "messages.claims.tool.selection-too-large");

            var r = TextReplacementConfig.builder().matchLiteral("%selection_limit%").replacement(Integer.toString(selectionSizeLimit));
            player.sendMessage(m.replaceText(r.build()));
            Sounds.playSound(player, Sounds.MISTAKE);
            return;
        }

        int count = 0;
        for (int z = sz; z <= ez; z++) {
            for (int x = sx; x <= ex; x++) {
                Chunk chunk = world.getChunkAt(x, z);
                // If someone else's claim is in this selection, fail
                if (!ClaimManager.isNotClaimedOrOwn(team, chunk)) {
                    player.sendMessage(Message.msg(player, "messages.claims.tool.other-claim-in-selection"));
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
        player.sendMessage(
                buildRemoveSuccessMessage(player, team)
        );
    }

    private static void tryClaimArea(@NotNull Player player, @NotNull Team team, @NotNull Location from, @NotNull Location to, boolean force) {
        int sx = Math.min(from.getChunk().getX(), to.getChunk().getX());
        int sz = Math.min(from.getChunk().getZ(), to.getChunk().getZ());
        int ex = Math.max(from.getChunk().getX(), to.getChunk().getX());
        int ez = Math.max(from.getChunk().getZ(), to.getChunk().getZ());

        World world = from.getWorld();
        if (world == null) return;

        if (
                ex-sx >= selectionSizeLimit ||
                        ez-sz >= selectionSizeLimit
        ) {
            var m = Message.msg(player, "messages.claims.tool.selection-too-large");

            var r = TextReplacementConfig.builder().matchLiteral("%selection_limit%").replacement(Integer.toString(selectionSizeLimit));
            player.sendMessage(m.replaceText(r.build()));

            return;
        }

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
                    if (chunksLeft < 0) {
                        continue;
                    }

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

        if (chunksLeft > 0) {
            int had = claimManager.getEntryOrNew(team).chunksLeft;
            claimManager.getEntryOrNew(team).chunksLeft -= count;
        }

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