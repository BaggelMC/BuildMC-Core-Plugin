package net.mathias2246.buildmc.claims.protections.entities;

import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.claims.protections.ProtectionConfig;
import net.mathias2246.buildmc.claims.protections.ProtectionUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.SulfurCube;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

@ProtectionConfig(value = "claims.protections.interactions.sulfur-cube-change")
public class SulfurCubeChange extends Protection {

    public SulfurCubeChange(@Nullable ConfigurationSection section) {
        //noinspection SimplifiableConditionalExpression
        super(Objects.requireNonNull(NamespacedKey.fromString("buildmc:sulfur_cube_change")), (section != null ? section.getBoolean("default", true) : true), section != null && section.getBoolean("is-hidden", false));
    }

    @Override
    public @NonNull String getTranslationBaseKey() {
        return "claims.protections.sulfur-cube-change";
    }

    @Override
    public @NotNull ItemStack getDisplay(@NotNull Player uiHolder) {

        String t = getTranslationBaseKey();

        return ProtectionUtil.createDisplayItem(uiHolder, Material.SULFUR_CUBE_BUCKET, t);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSulfurCubeInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof SulfurCube)) return;

        Player player = event.getPlayer();
        ProtectionUtil.handleProtection(event, this, event.getRightClicked().getLocation(), player);
    }
}
