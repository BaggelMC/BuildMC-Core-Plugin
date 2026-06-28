package net.mathias2246.buildmc.claims.protections.misc;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.claims.protections.ProtectionUtil;
import net.mathias2246.buildmc.util.AudienceUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public class ProjectileInteractions extends Protection {
    public ProjectileInteractions(@Nullable ConfigurationSection section) {
        //noinspection SimplifiableConditionalExpression
        super(Objects.requireNonNull(NamespacedKey.fromString("buildmc:projectile_interactions")), (section != null ? section.getBoolean("default", true) : true), section != null && section.getBoolean("is-hidden", false));
    }

    @Override
    public @NonNull String getTranslationBaseKey() {
        return "claims.protections.projectile-interactions";
    }

    @Override
    public @NotNull ItemStack getDisplay(@NotNull Player uiHolder) {
        String t = getTranslationBaseKey();

        return ProtectionUtil.createDisplayItem(uiHolder, Material.ARROW, t);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntityType() == EntityType.WIND_CHARGE) return;
        Block block = event.getHitBlock();
        if (block == null) return;

        Projectile projectile = event.getEntity();
        ProjectileSource projectileSource = projectile.getShooter();

        if (!(projectileSource instanceof Player player)) return;

        if (!ClaimManager.isPlayerAllowed(player, getKey(), block.getLocation())) {
            projectile.remove();
            event.setCancelled(true);
            AudienceUtil.sendActionBar(player, Component.translatable(getTranslationBaseKey()+".message"));
            returnProjectileToPlayer(player, projectile);
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectileInteract(EntityInteractEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof Projectile projectile)) return;
        if (event.getEntityType() == EntityType.WIND_CHARGE) return;

        ProjectileSource shooter = projectile.getShooter();
        if (!(shooter instanceof Player player)) return;

        if (!ClaimManager.isPlayerAllowed(player, getKey(), event.getBlock().getLocation())) {
            event.setCancelled(true);
            // Message sending should already be handled by onProjectileHit()
        }
    }

    private void returnProjectileToPlayer(Player player, Projectile projectile) {
        GameMode gameMode = player.getGameMode();
        if (gameMode != GameMode.SURVIVAL && gameMode != GameMode.ADVENTURE) return;

        ItemStack item = getProjectileItem(projectile);
        if (item == null) return;

        player.getInventory().addItem(item).forEach((slot, leftover) ->
                player.getWorld().dropItemNaturally(player.getLocation(), leftover)
        );
    }

    private @Nullable ItemStack getProjectileItem(Projectile projectile) {
        // Covers: arrow, tipped arrow, spectral arrow
        if (projectile instanceof Arrow arrow) {
            return arrow.getItemStack().clone();
        }

        // Trident
        if (projectile instanceof Trident trident) {
            return trident.getItemStack().clone();
        }

        // TODO: Snowball, Egg, Ender Pearl, Splash/Lingering Potion, Xp Bottle

        return null;
    }
}
