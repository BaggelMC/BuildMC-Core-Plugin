package net.mathias2246.buildmc.claims.protections.entities;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.claims.protections.ProtectionUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class EntityPlace extends Protection {
    public EntityPlace(@Nullable ConfigurationSection section) {
        //noinspection SimplifiableConditionalExpression
        super(Objects.requireNonNull(NamespacedKey.fromString("buildmc:entity_place")), (section != null ? section.getBoolean("default", true) : true), section != null && section.getBoolean("is-hidden", false));
    }

    @Override
    public @NotNull GuiItem getDisplay(@NotNull Player uiHolder, @NotNull Gui gui) {

        String t = getTranslationBaseKey();

        return ProtectionUtil.createDisplayItem(uiHolder, Material.COW_SPAWN_EGG, t);
    }

    @Override
    public String getTranslationBaseKey() {
        return "claims.flags.player-place-entity";
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityPlace(EntityPlaceEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getEntity();

        if (player == null) return;

        if (!ClaimManager.isPlayerAllowed(player, getKey(), entity.getLocation())) {
            event.setCancelled(true);
            CoreMain.plugin.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.entity-place"));
        }
    }
}
