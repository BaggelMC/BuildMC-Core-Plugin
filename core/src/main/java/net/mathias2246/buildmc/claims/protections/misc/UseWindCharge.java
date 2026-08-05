package net.mathias2246.buildmc.claims.protections.misc;

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent;
import io.papermc.paper.event.entity.EntityKnockbackEvent;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.claims.protections.ProtectionConfig;
import net.mathias2246.buildmc.claims.protections.ProtectionUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.*;

@ProtectionConfig(value = "claims.protections.wind-charge")
public class UseWindCharge extends Protection {

    // Tracks wind charges that should be fully suppressed
    private final Set<UUID> cancelledWindCharges = Collections.newSetFromMap(new WeakHashMap<>());

    public UseWindCharge(@Nullable ConfigurationSection section) {
        //noinspection SimplifiableConditionalExpression
        super(Objects.requireNonNull(NamespacedKey.fromString("buildmc:wind_charges")), (section != null ? section.getBoolean("default", true) : true), section != null && section.getBoolean("is-hidden", false));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWindChargeExplode(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof WindCharge windCharge)) return;

        Location location = event.getLocation();

        Claim claim = ClaimManager.getClaim(location);
        if (claim == null) return;

        if (!claim.hasProtection(getKey())) return;

        ProjectileSource shooter = windCharge.getShooter();
        if (shooter instanceof Player player && ClaimManager.isPlayerAllowed(player, getKey(), location)) return;

        this.cancelledWindCharges.add(windCharge.getUniqueId());
        windCharge.setYield(0);
        event.blockList().clear();
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof WindCharge windCharge)) return;

        if (cancelledWindCharges.remove(windCharge.getUniqueId())) {
            windCharge.setYield(0);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onKockback(EntityKnockbackByEntityEvent event) {

        Location location = event.getEntity().getLocation();
        if (!(event.getHitBy() instanceof Player player) || !(event.getCause() == EntityKnockbackEvent.Cause.EXPLOSION)) return;

        if (!ClaimManager.isPlayerAllowed(player, getKey(), location)) {
            event.setCancelled(true);
        }
    }

    @Override
    public @NotNull ItemStack getDisplay(@NotNull Player uiHolder) {
        return ProtectionUtil.createDisplayItem(uiHolder, Material.WIND_CHARGE, getTranslationBaseKey());
    }

    @Override
    public @NonNull String getTranslationBaseKey() {
        return "claims.protections.wind-charge";
    }
}