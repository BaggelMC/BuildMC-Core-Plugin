package net.mathias2246.buildmc.claims.protections.entities;

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent;
import io.papermc.paper.event.entity.EntityPushedByEntityAttackEvent;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.claims.protections.ProtectionUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

// WIP, so not registered in game
public class EntityPush extends Protection {
    public EntityPush(@Nullable ConfigurationSection section) {
        //noinspection SimplifiableConditionalExpression
        super(Objects.requireNonNull(NamespacedKey.fromString("buildmc:entity_push")), (section != null ? section.getBoolean("default", true) : true), section != null && section.getBoolean("is-hidden", false));
    }

    @Override
    public @NotNull ItemStack getDisplay(@NotNull Player uiHolder) {

        String t = getTranslationBaseKey();

        return ProtectionUtil.createDisplayItem(uiHolder, Material.COW_SPAWN_EGG, t);
    }

    @Override
    public @NonNull String getTranslationBaseKey() {
        return "claims.protections.player-push-entity";
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityPush(EntityKnockbackByEntityEvent event) {
        System.out.println("Get Hit By: " + event.getHitBy());
        if (!(event.getHitBy() instanceof Player player)) return;

        ProtectionUtil.handleProtection(event, this, event.getEntity().getLocation(), player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityAttackPush(EntityPushedByEntityAttackEvent event) {
        if (!(event.getPushedBy() instanceof Player player)) return;

        ProtectionUtil.handleProtection(event, this, event.getEntity().getLocation(), player);
    }
}