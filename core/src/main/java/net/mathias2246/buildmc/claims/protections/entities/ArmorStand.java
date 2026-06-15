package net.mathias2246.buildmc.claims.protections.entities;

import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.claims.protections.ProtectionUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public class ArmorStand extends Protection {

    public ArmorStand(@Nullable ConfigurationSection section) {
        //noinspection SimplifiableConditionalExpression
        super(Objects.requireNonNull(NamespacedKey.fromString("buildmc:armor_stand_manipulation")), (section != null ? section.getBoolean("default", true) : true), section != null && section.getBoolean("is-hidden", false));
    }

    @Override
    public @NonNull String getTranslationBaseKey() {
        return "claims.protections.interaction-armor-stand";
    }

    @Override
    public @NotNull ItemStack getDisplay(@NotNull Player uiHolder, @NotNull Gui gui) {
        String t = getTranslationBaseKey();

        return ProtectionUtil.createDisplayItem(uiHolder, Material.ARMOR_STAND, t);
    }

    @EventHandler
    public void onPlayerChangeArmorStand(PlayerArmorStandManipulateEvent event) {
        ProtectionUtil.handleProtection(event, this, event.getRightClicked().getLocation(), event.getPlayer());
    }
}
