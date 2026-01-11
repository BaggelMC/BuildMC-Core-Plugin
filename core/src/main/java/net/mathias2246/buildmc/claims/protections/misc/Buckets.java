package net.mathias2246.buildmc.claims.protections.misc;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.claims.protections.ProtectionUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBucketEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

        if (!ClaimManager.isPlayerAllowed(player, getKey(), block.getLocation())) {
            CoreMain.plugin.sendPlayerActionBar(player,
                    Component.translatable("messages.claims.not-accessible.block-place"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityPickup(PlayerBucketEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getEntity();

        if (!ClaimManager.isPlayerAllowed(player, getKey(), entity.getLocation())) {
            CoreMain.plugin.sendPlayerActionBar(player,
                    Component.translatable("messages.claims.not-accessible.entity-bucket"));
            event.setCancelled(true);
        }
    }

    @Override
    public String getTranslationBaseKey() {
        return "claims.flags.bucket-usage";
    }

    @Override
    public @NotNull GuiItem getDisplay(@NotNull Player uiHolder, @NotNull Gui gui) {
        String t = getTranslationBaseKey();

        return ProtectionUtil.createDisplayItem(uiHolder, Material.BUCKET, t);
    }
}
