package net.mathias2246.buildmc.claims.protections.misc;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.api.item.ItemUtil;
import net.mathias2246.buildmc.claims.protections.ProtectionUtil;
import net.mathias2246.buildmc.ui.UIUtil;
import net.mathias2246.buildmc.util.ComponentUtil;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public class PotionSplash extends Protection {
    public PotionSplash(@Nullable ConfigurationSection section) {
        //noinspection SimplifiableConditionalExpression
        super(Objects.requireNonNull(NamespacedKey.fromString("buildmc:splash_potion")), (section != null ? section.getBoolean("default", true) : true), section != null && section.getBoolean("is-hidden", false));
    }

    @Override
    public @NonNull String getTranslationBaseKey() {
        return "claims.protections.splash-potions";
    }

    @Override
    public @NotNull ItemStack getDisplay(@NotNull Player uiHolder, @NotNull Gui gui) {
        String translationBaseKey = getTranslationBaseKey();

        ItemStack displayBase = new ItemStack(Material.STICK);

        ItemUtil.editMeta(displayBase, (meta) -> {
            meta.setItemModel(NamespacedKey.fromString("minecraft:splash_potion"));

            Component name = Message.msg(uiHolder, translationBaseKey+".name")
                            .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);

            meta.itemName(
                    name
            );
            meta.addItemFlags(
                    ItemFlag.HIDE_ATTRIBUTES
            );

            meta.lore(ComponentUtil.splitComponentByNewline(Message.msg(uiHolder, translationBaseKey + ".lore")));
        });

        return displayBase;
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPotionSplash(PotionSplashEvent event) {
        ThrownPotion thrownPotion = event.getPotion();
        if (thrownPotion.getShooter() instanceof Player player) {
            ProtectionUtil.handleProtection(event, this, thrownPotion.getLocation(), player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLingeringPotionSplash(LingeringPotionSplashEvent event) {
        ThrownPotion thrownPotion = event.getEntity();
        if (thrownPotion.getShooter() instanceof Player player) {
            ProtectionUtil.handleProtection(event, this, thrownPotion.getLocation(), player);
        }
    }

}
