package net.mathias2246.buildmc.claims.protections.misc;

import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.claims.protections.ProtectionUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;

public class Explosion extends Protection {
    public Explosion(@Nullable ConfigurationSection section) {
        //noinspection SimplifiableConditionalExpression
        super(Objects.requireNonNull(NamespacedKey.fromString("buildmc:block_explosions")), (section != null ? section.getBoolean("default", true) : true), section != null && section.getBoolean("is-hidden", false));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplosion(EntityExplodeEvent event) {
        if (event.getEntityType() == EntityType.WIND_CHARGE) return;
        filterBlockList(event.blockList());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplosion(BlockExplodeEvent event) {
        Location location = event.getBlock().getLocation();

        Claim claim = ClaimManager.getClaim(location);
        if (claim == null) {
            return;
        }

        if (claim.hasProtection(getKey())) {
            event.blockList().clear();
        }
    }


    private void filterBlockList(List<Block> blocks) {
        blocks.removeIf(block -> {
            Claim claim = ClaimManager.getClaim(block.getChunk().getBlock(0, 0, 0).getLocation());
            return claim != null && claim.hasProtection(getKey());
        });
    }

    @Override
    public @NotNull ItemStack getDisplay(@NotNull Player uiHolder) {

        String t = getTranslationBaseKey();

        return ProtectionUtil.createDisplayItem(uiHolder, Material.TNT, t);
    }

    @Override
    public @NonNull String getTranslationBaseKey() {
        return "claims.protections.explosion-block-damage";
    }
}
