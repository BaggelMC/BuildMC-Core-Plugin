package net.mathias2246.buildmc.claims.protections.blocks;

import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.claims.protections.ProtectionConfig;
import net.mathias2246.buildmc.claims.protections.ProtectionUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

@ProtectionConfig(value = "claims.protections.water-flow", defaultEnabled = true)
public class WaterFlow extends Protection {
    public WaterFlow(@Nullable ConfigurationSection section) {
        //noinspection SimplifiableConditionalExpression
        super(Objects.requireNonNull(NamespacedKey.fromString("buildmc:water_flow")), (section != null ? section.getBoolean("default", true) : true), section != null && section.getBoolean("is-hidden", false));
    }

    @Override
    public @NonNull String getTranslationBaseKey() {
        return "claims.protections.water-flow";
    }

    @Override
    public @NotNull ItemStack getDisplay(@NotNull Player uiHolder) {
        String t = getTranslationBaseKey();

        return ProtectionUtil.createDisplayItem(uiHolder, Material.WATER_BUCKET, t);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWaterFlow(BlockFromToEvent event) {
        Block fromBlock = event.getBlock();
        Block toBlock = event.getToBlock();

        if (!isWater(fromBlock)) return;

        Claim toClaim = ClaimManager.getClaim(toBlock.getLocation());
        if (toClaim == null) return;

        Claim fromClaim = ClaimManager.getClaim(fromBlock.getLocation());
        if (toClaim.equals(fromClaim)) return;

        if (toClaim.hasProtection(this)) {
            event.setCancelled(true);
        }
    }

    private boolean isWater(Block block) {
        if (block.getType() == Material.WATER || block.getType() == Material.BUBBLE_COLUMN) return true;

        if (block.getBlockData() instanceof org.bukkit.block.data.Waterlogged waterlogged) {
            return waterlogged.isWaterlogged();
        }
        return false;
    }

}
