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
import org.bukkit.event.entity.EntityTameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class EntityTame extends Protection {

    public EntityTame(@Nullable ConfigurationSection section) {
        //noinspection SimplifiableConditionalExpression
        super(Objects.requireNonNull(NamespacedKey.fromString("buildmc:entity_tame")), (section != null ? section.getBoolean("default", true) : true), section != null && section.getBoolean("is-hidden", false));
    }



    @Override
    public @NotNull GuiItem getDisplay(@NotNull Player uiHolder, @NotNull Gui gui) {

        String t = getTranslationBaseKey();

        return ProtectionUtil.createDisplayItem(uiHolder, Material.BONE, t);
    }

    @Override
    public String getTranslationBaseKey() {
        return "claims.flags.interaction-tame-entity";
    }

    @EventHandler
    public void onTame(EntityTameEvent event) {
        if (!(event.getOwner() instanceof Player player)) return;
        Entity entity = event.getEntity();

        if (!ClaimManager.isPlayerAllowed(player, getKey(), entity.getLocation())) {
            CoreMain.plugin.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.tame-entity"));
            event.setCancelled(true);
        }
    }

}
