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
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SignEdit extends Protection {
    public SignEdit(@Nullable ConfigurationSection section) {
        //noinspection SimplifiableConditionalExpression
        super(Objects.requireNonNull(NamespacedKey.fromString("buildmc:sign_edit")), (section != null ? section.getBoolean("default", true) : true), section != null && section.getBoolean("is-hidden", false));
    }

    @Override
    public String getTranslationBaseKey() {
        return "claims.flags.sign-editing";
    }

    @Override
    public @NotNull GuiItem getDisplay(@NotNull Player uiHolder, @NotNull Gui gui) {
        String t = getTranslationBaseKey();

        return ProtectionUtil.createDisplayItem(uiHolder, Material.OAK_SIGN, t);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignChange(SignChangeEvent event) {

        Player player = event.getPlayer();

        if (!ClaimManager.isPlayerAllowed(player, getKey(), event.getBlock().getLocation())) {
            // Cancel the sign change
            event.setCancelled(true);

            if (event.getBlock().getState() instanceof Sign sign) {
                sign.update(); // Reset visual state
            }

            // Send feedback to the player
            CoreMain.plugin.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.sign"));
        }
    }
}
