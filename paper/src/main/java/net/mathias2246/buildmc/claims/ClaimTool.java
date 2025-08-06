package net.mathias2246.buildmc.claims;


import com.destroystokyo.paper.ParticleBuilder;
import net.kyori.adventure.text.Component;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static net.mathias2246.buildmc.Main.config;
import static net.mathias2246.buildmc.Main.plugin;

@SuppressWarnings("UnstableApiUsage")
public class ClaimTool implements Listener {

    public static final @NotNull NamespacedKey CLAIM_TOOL_ITEM_PDC_KEY = Objects.requireNonNull(NamespacedKey.fromString("buildmc:is_claim_tool_item"));

    public static final @NotNull NamespacedKey REMOVE_TOOL_ITEM_PDC_KEY = Objects.requireNonNull(NamespacedKey.fromString("buildmc:is_remove_tool_item"));

    public static int selectionSizeLimit = 8;

    public static ItemStack claimToolItemstack;

    public static void setup() {
        var claimToolItem = Material.getMaterial(config.getString("claims.tool.tool-item", "carrot_on_a_stick").toUpperCase());
        if (claimToolItem == null) claimToolItem = Material.CARROT_ON_A_STICK;

        claimToolItemstack = new ItemStack(claimToolItem);

        selectionSizeLimit = config.getInt("claims.tool.limit-selection", 8);
        if (selectionSizeLimit < 0) selectionSizeLimit = Integer.MAX_VALUE;

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
    public void onPlayerUseTool(PlayerInteractEvent event) {
        event.setCancelled(true);
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (!isClaimTool(item)) return;

        if (player.hasCooldown(item)) {
            player.sendMessage(Component.translatable("messages.claims.tool.tool-cooldown"));
            return;
        }

        Location at = (event.getClickedBlock() != null)
                ? event.getClickedBlock().getLocation()
                : player.getLocation();

        boolean isLeftClick = event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK;
        boolean isRightClick = event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK;

        if (isLeftClick) {
            player.setMetadata(
                    "claim_tool_pos1",
                    new FixedMetadataValue(plugin, LocationUtil.serialize(at))
            );
            player.sendMessage(Message.msg(player, "messages.claims.tool.set-pos1"));
            Sounds.playSound(player, Sounds.SUCCESS);

        } else if (isRightClick) {
            player.setMetadata(
                    "claim_tool_pos2",
                    new FixedMetadataValue(plugin, LocationUtil.serialize(at))
            );
            player.sendMessage(Message.msg(player, "messages.claims.tool.set-pos2"));
            Sounds.playSound(player, Sounds.SUCCESS);
        }

        // Set cooldown
        player.setCooldown(item, 60);

        // Optional: tell them if both positions are now set
        if (player.hasMetadata("claim_tool_pos1") && player.hasMetadata("claim_tool_pos2")) {
            player.sendMessage(Message.msg(player, "messages.claims.tool.both-positions-set"));
        }
    }
}
