package net.mathias2246.buildmc.claims.protections.misc;

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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class FrostWalker extends Protection {
    public FrostWalker(@Nullable ConfigurationSection section) {
        //noinspection SimplifiableConditionalExpression
        super(Objects.requireNonNull(NamespacedKey.fromString("buildmc:frost_walker")), (section != null ? section.getBoolean("default", true) : true), section != null && section.getBoolean("is-hidden", false));
    }

    @Override
    public @NotNull GuiItem getDisplay(@NotNull Player uiHolder, @NotNull Gui gui) {

        String t = getTranslationBaseKey();

        return ProtectionUtil.createDisplayItem(uiHolder, Material.ICE, t);
    }

    @Override
    public String getTranslationBaseKey() {
        return "claims.flags.frost-walker";
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFrostWalkerUse(EntityBlockFormEvent event) {
        if (!event.getNewState().getType().equals(Material.FROSTED_ICE)) return;

        Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;

        ItemStack boots = player.getInventory().getBoots();
        if (boots == null || !boots.containsEnchantment(Enchantment.FROST_WALKER)) return;

        if (!ClaimManager.isPlayerAllowed(player, getKey(), event.getBlock().getLocation())) {
            event.setCancelled(true);
            CoreMain.plugin.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.frostwalker"));
        }
    }
}
