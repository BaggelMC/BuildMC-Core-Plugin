package net.mathias2246.buildmc.claims.protections.misc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.api.item.ItemUtil;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.inventoryframework.gui.GuiItem;
import net.mathias2246.buildmc.inventoryframework.gui.type.util.Gui;
import net.mathias2246.buildmc.ui.UIUtil;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class ProjectileInteractions extends Protection {
    public ProjectileInteractions(@Nullable ConfigurationSection section) {
        super(Objects.requireNonNull(NamespacedKey.fromString("buildmc:projectile_interactions")), (section != null ? section.getBoolean("default", true) : true), section != null && section.getBoolean("is-hidden", false));
    }

    @Override
    public String getTranslationBaseKey() {
        return "claims.flags.projectile-interactions";
    }

    @Override
    public @NotNull GuiItem getDisplay(@NotNull Player uiHolder, @NotNull Gui gui) {
        String t = getTranslationBaseKey();

        ItemStack displayBase = new ItemStack(Material.ARROW);
        ItemUtil.editMeta(displayBase, (meta) -> {
            meta.setItemName(LegacyComponentSerializer.legacySection().serialize(
                    Message.msg(uiHolder, t+".name")
            ));
            meta.addItemFlags(
                    ItemFlag.HIDE_ATTRIBUTES,
                    ItemFlag.HIDE_ADDITIONAL_TOOLTIP
            );
            meta.setLore(List.of(LegacyComponentSerializer.legacySection().serialize(Message.msg(uiHolder, t + ".lore")).split("\n")));
        });

        return new GuiItem(
                displayBase,
                UIUtil.noInteract
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProjectileHit(ProjectileHitEvent event) {
        Block block = event.getHitBlock();
        if (block == null) return;

        Projectile projectile = event.getEntity();
        ProjectileSource projectileSource = projectile.getShooter();

        if (!(projectileSource instanceof Player player)) return;

        if (!ClaimManager.isPlayerAllowed(player, getKey(), block.getLocation())) {
            projectile.remove();
            event.setCancelled(true);
            CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.interact"));
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectileInteract(EntityInteractEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof Projectile projectile)) return;

        ProjectileSource shooter = projectile.getShooter();
        if (!(shooter instanceof Player player)) return;

        if (!ClaimManager.isPlayerAllowed(player, getKey(), event.getBlock().getLocation())) {
            event.setCancelled(true);
            // Message sending should already be handled by onProjectileHit()
        }
    }
}
