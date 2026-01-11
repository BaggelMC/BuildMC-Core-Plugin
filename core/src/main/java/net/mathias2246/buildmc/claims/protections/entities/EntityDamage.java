package net.mathias2246.buildmc.claims.protections.entities;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.claims.protections.ProtectionUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class EntityDamage extends Protection {

    public EntityDamage(@Nullable ConfigurationSection section) {
        //noinspection SimplifiableConditionalExpression
        super(Objects.requireNonNull(NamespacedKey.fromString("buildmc:entity_damage")), (section != null ? section.getBoolean("default", true) : true), section != null && section.getBoolean("is-hidden", false));
    }

    @Override
    public String getTranslationBaseKey() {
        return "claims.flags.entity-damage";
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();
        Entity damager = event.getDamageSource().getCausingEntity();

        if ((victim instanceof Player a)) return;
        if (!(damager instanceof Player attacker)) return;

        Claim claim = ClaimManager.getClaim(victim.getLocation());

        if (claim == null) return;

        if (claim.getWhitelistedPlayers().contains(attacker.getUniqueId())) return;

        NamespacedKey k = getKey();

        if (claim.hasProtection(k) && !ClaimManager.isPlayerAllowed(attacker, k, claim)) {
            CoreMain.plugin.sendPlayerActionBar(attacker, Component.translatable("messages.claims.not-accessible.entity-damage"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(VehicleDamageEvent event) {
        Vehicle victim = event.getVehicle();
        Entity damager = event.getAttacker();
        
        if (!(damager instanceof Player attacker)) return;

        if (!ClaimManager.isPlayerAllowed(attacker, getKey(), victim.getLocation())) {
            CoreMain.plugin.sendPlayerActionBar(attacker, Component.translatable("messages.claims.not-accessible.entity-damage"));
            event.setCancelled(true);
        }
    }

    @Override
    public @NotNull GuiItem getDisplay(@NotNull Player uiHolder, @NotNull Gui gui) {
        String t = getTranslationBaseKey();

        return ProtectionUtil.createDisplayItem(uiHolder, Material.IRON_SWORD, t);
    }
}
