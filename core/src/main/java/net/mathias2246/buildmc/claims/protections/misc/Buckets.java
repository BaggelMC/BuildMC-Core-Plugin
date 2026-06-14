package net.mathias2246.buildmc.claims.protections.misc;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.claims.protections.ProtectionUtil;
import net.mathias2246.buildmc.util.AudienceUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.logging.Level;

public class Buckets extends Protection {

    private final java.util.Map<java.util.UUID, Integer> pendingSequences = new java.util.concurrent.ConcurrentHashMap<>();

    public Buckets(@Nullable ConfigurationSection section) {
        //noinspection SimplifiableConditionalExpression
        super(Objects.requireNonNull(NamespacedKey.fromString("buildmc:bucket_usage")), (section != null ? section.getBoolean("default", true) : true), section != null && section.getBoolean("is-hidden", false));
    }

    @Override
    public @NonNull String getTranslationBaseKey() {
        return "claims.protections.bucket-usage";
    }

    @Override
    public @NotNull ItemStack getDisplay(@NotNull Player uiHolder, @NotNull Gui gui) {
        String t = getTranslationBaseKey();

        return ProtectionUtil.createDisplayItem(uiHolder, Material.BUCKET, t);
    }

    public static class BucketPacketListener extends PacketAdapter {

        private final @NotNull Buckets protection;
        private final @NotNull Plugin plugin;

        public BucketPacketListener(@NotNull Plugin plugin, @NotNull Buckets protection) {
            super(plugin, ListenerPriority.NORMAL,
                    PacketType.Play.Client.USE_ITEM_ON,
                    PacketType.Play.Client.USE_ITEM
            );
            this.protection = protection;
            this.plugin = plugin;
        }

        private final java.util.Set<java.util.UUID> cancelNextUseItem = java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());

        @Override
        public void onPacketReceiving(@NotNull PacketEvent event) {
            Player player = event.getPlayer();

            var hand = event.getPacket().getHands().read(0);

            ItemStack item = switch (hand) {
                case MAIN_HAND -> player.getInventory().getItemInMainHand();
                case OFF_HAND  -> player.getInventory().getItemInOffHand();
            };

            if (!isBucket(item)) return;

            if (event.getPacketType() == PacketType.Play.Client.USE_ITEM) {
                if (cancelNextUseItem.remove(player.getUniqueId())) {
                    event.setCancelled(true);

                    final int useItemSequence = event.getPacket().getIntegers().read(0);
                    Bukkit.getScheduler().runTask(plugin, () ->
                            sendBlockChangedAck(player, useItemSequence)
                    );
                }
                return;
            }

            var hitResult = event.getPacket().getStructures().read(0);
            var pos = hitResult.getBlockPositionModifier().read(0);
            var hitFace = toBlockFace(hitResult.getDirections().read(0));

            var hitBlock = player.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
            var placementBlock = hitBlock.getRelative(hitFace);

            Claim claim = ClaimManager.getClaim(placementBlock.getLocation());
            if (claim == null) return;

            if (!ClaimManager.isPlayerAllowed(player, protection.getKey(), claim)) {
                event.setCancelled(true);
                cancelNextUseItem.add(player.getUniqueId());
                AudienceUtil.sendActionBar(player, Component.translatable(protection.getTranslationBaseKey() + ".message"));

                final int useItemOnSequence = event.getPacket().getIntegers().read(0);
                final org.bukkit.block.Block finalPlacementBlock = placementBlock;
                final org.bukkit.block.Block finalHitBlock = hitBlock;

                Bukkit.getScheduler().runTask(plugin, () -> {
                    sendBlockChangedAck(player, useItemOnSequence);
                    resyncBlock(player, finalPlacementBlock);
                    resyncBlock(player, finalHitBlock);
                });
            }
        }

        private void sendBlockChangedAck(@NotNull Player player, int sequence) {
            PacketContainer ack = ProtocolLibrary.getProtocolManager()
                    .createPacket(PacketType.Play.Server.BLOCK_CHANGED_ACK);
            ack.getIntegers().write(0, sequence);
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, ack);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to send BlockChangedAck for " + player.getName(), e);
            }
        }

        private void resyncBlock(@NotNull Player player, @NotNull org.bukkit.block.Block block) {
            PacketContainer packet = ProtocolLibrary.getProtocolManager()
                    .createPacket(PacketType.Play.Server.BLOCK_CHANGE);

            packet.getBlockPositionModifier().write(0,
                    new BlockPosition(block.getX(), block.getY(), block.getZ()));

            packet.getBlockData().write(0,
                    WrappedBlockData.createData(block.getBlockData()));

            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to resync block for " + player.getName(), e);
            }
        }

        private org.bukkit.block.BlockFace toBlockFace(com.comphenix.protocol.wrappers.EnumWrappers.Direction direction) {
            return switch (direction) {
                case NORTH -> org.bukkit.block.BlockFace.NORTH;
                case SOUTH -> org.bukkit.block.BlockFace.SOUTH;
                case EAST  -> org.bukkit.block.BlockFace.EAST;
                case WEST  -> org.bukkit.block.BlockFace.WEST;
                case UP    -> org.bukkit.block.BlockFace.UP;
                case DOWN  -> org.bukkit.block.BlockFace.DOWN;
            };
        }

        private boolean isBucket(@NotNull ItemStack item) {
            return switch (item.getType()) {
                case BUCKET,
                     WATER_BUCKET,
                     LAVA_BUCKET,
                     MILK_BUCKET,
                     POWDER_SNOW_BUCKET,
                     AXOLOTL_BUCKET,
                     COD_BUCKET,
                     SALMON_BUCKET,
                     TROPICAL_FISH_BUCKET,
                     TADPOLE_BUCKET,
                     PUFFERFISH_BUCKET -> true;
                default -> false;
            };
        }
    }
}
