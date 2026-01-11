package net.mathias2246.buildmc.claims.protections.entities;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.claims.protections.ProtectionUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class EntityBlockModifications extends Protection {

    public EntityBlockModifications(@Nullable ConfigurationSection section) {
        //noinspection SimplifiableConditionalExpression
        super(Objects.requireNonNull(NamespacedKey.fromString("buildmc:entity_block_modifications")), (section != null ? section.getBoolean("default", true) : true), section != null && section.getBoolean("is-hidden", false));
    }

    @Override
    public @NotNull GuiItem getDisplay(@NotNull Player uiHolder, @NotNull Gui gui) {

        String t = getTranslationBaseKey();

        return ProtectionUtil.createDisplayItem(uiHolder, Material.WITHER_SKELETON_SKULL, t);
    }

    @Override
    public String getTranslationBaseKey() {
        return "claims.flags.entity-modifications";
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        EntityType entityType = event.getEntityType();

        if (!(entityType.equals(EntityType.WITHER) || entityType.equals(EntityType.ENDERMAN) || entityType.equals(EntityType.RAVAGER))) return;

        Location location = event.getBlock().getLocation();

        Claim claim = ClaimManager.getClaim(location);

        if (claim == null) return;

        if (claim.hasProtection(getKey())) event.setCancelled(true);
    }
}
