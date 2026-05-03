package net.mathias2246.buildmc.claims.protections.misc;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.claims.protections.ProtectionUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public class Buckets extends Protection {

    public Buckets(@Nullable ConfigurationSection section) {
        //noinspection SimplifiableConditionalExpression
        super(Objects.requireNonNull(NamespacedKey.fromString("buildmc:bucket_usage")), (section != null ? section.getBoolean("default", true) : true), section != null && section.getBoolean("is-hidden", false));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucketUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null) return;

        Material type = item.getType();
        Block block = event.getClickedBlock();

        if (block == null) return;

        boolean isBucketUse = switch (type) {
            case WATER_BUCKET, LAVA_BUCKET, POWDER_SNOW_BUCKET, BUCKET,
                 AXOLOTL_BUCKET, TADPOLE_BUCKET, PUFFERFISH_BUCKET,
                 TROPICAL_FISH_BUCKET, SALMON_BUCKET, COD_BUCKET -> true;
            default -> false;
        };

        if (!isBucketUse) return;

        ProtectionUtil.handleProtection(event, this, block.getLocation(), player);
    }

    @Override
    public @NonNull String getTranslationBaseKey() {
        return "claims.protections.bucket-usage";
    }

    @Override
    public @NotNull GuiItem getDisplay(@NotNull Player uiHolder, @NotNull Gui gui) {
        String t = getTranslationBaseKey();

        return ProtectionUtil.createDisplayItem(uiHolder, Material.BUCKET, t);
    }

    public static class BucketPacketListener extends PacketAdapter {

        private final @NotNull Buckets protection;

        public BucketPacketListener(@NotNull Plugin plugin, @NotNull Buckets protection) {
            super(plugin, ListenerPriority.NORMAL,
                    PacketType.Play.Client.USE_ITEM
            );
            this.protection = protection;
        }

        @Override
        public void onPacketReceiving(@NotNull PacketEvent event) {
            Player player = event.getPlayer();

            // Get the item the player is holding in the hand they used
            // Hand is stored in the first enum modifier (0 = MAIN_HAND, 1 = OFF_HAND)
            var hand = event.getPacket()
                    .getHands()
                    .read(0);

            ItemStack item = switch (hand) {
                case MAIN_HAND -> player.getInventory().getItemInMainHand();
                case OFF_HAND  -> player.getInventory().getItemInOffHand();
            };

            if (!isBucket(item)) return;

            // Ray cast from the player's eye position up to their interaction reach
            var rayTrace = player.getWorld().rayTraceBlocks(
                    player.getEyeLocation(),
                    player.getEyeLocation().getDirection(),
                    Objects.requireNonNull(player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE)).getValue()
                    // Gets the players interaction range
            );

            if (rayTrace == null) return; // Player is not looking at any block

            var hitBlock    = rayTrace.getHitBlock();
            var hitFace     = rayTrace.getHitBlockFace();

            if (hitBlock == null || hitFace == null) return;

            // The block where the bucket contents would be placed
            // (the face they're looking at, one block out)
            var placementLocation = hitBlock.getRelative(hitFace).getLocation();

            ProtectionUtil.handleProtection(event, protection, placementLocation, player);
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
