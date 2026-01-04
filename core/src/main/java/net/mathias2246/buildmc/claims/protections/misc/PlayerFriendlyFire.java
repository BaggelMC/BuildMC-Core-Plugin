package net.mathias2246.buildmc.claims.protections.misc;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.claims.Protection;
import net.mathias2246.buildmc.api.item.ItemUtil;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.ui.UIUtil;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class PlayerFriendlyFire extends Protection {

    public PlayerFriendlyFire(@Nullable ConfigurationSection section) {
        //noinspection SimplifiableConditionalExpression
        super(Objects.requireNonNull(NamespacedKey.fromString("buildmc:player_friendly_fire")),
                (section != null ? section.getBoolean("default", true) : true),
                section != null && section.getBoolean("is-hidden", false));
    }

    @Override
    public String getTranslationBaseKey() {
        return "claims.flags.players-friendly-fire";
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity rawDamager = event.getDamager();
        Player attacker = null;

        if (rawDamager instanceof Player p) {
            attacker = p;
        } else if (rawDamager instanceof Projectile proj) {
            if (proj.getShooter() instanceof Player shooter) {
                attacker = shooter;
            }
        }

        if (attacker == null) return;

        Entity victimEntity = event.getEntity();
        if (!(victimEntity instanceof Player victim)) return;

        Claim claim;
        try {
            claim = ClaimManager.getClaim(victim.getLocation());
        } catch (SQLException e) {
            CoreMain.plugin.getLogger().severe("SQL error while getting claim: " + e);
            return;
        }

        if (claim == null) return;

        boolean protectionEnabled = claim.hasProtection(getKey());

        if (!protectionEnabled) {
            // Protection flag OFF -> allowed players are protected from not-allowed attackers
            boolean victimAllowed = ClaimManager.isPlayerAllowed(victim, getKey(), victim.getLocation());
            boolean attackerAllowed = ClaimManager.isPlayerAllowed(attacker, getKey(), victim.getLocation());

            // If victim is allowed but attacker is not allowed, cancel the damage
            if (victimAllowed && !attackerAllowed) {
                event.setCancelled(true);
                CoreMain.plugin.sendPlayerActionBar(attacker, Component.translatable("messages.claims.not-accessible.entity-damage"));
            }
        }
    }

    @Override
    public @NotNull GuiItem getDisplay(@NotNull Player uiHolder, @NotNull Gui gui) {
        String t = getTranslationBaseKey();

        ItemStack displayBase = new ItemStack(Material.PLAYER_HEAD);
        ItemUtil.editMeta(displayBase, (meta) -> {
            meta.setItemName(LegacyComponentSerializer.legacySection().serialize(
                    Message.msg(uiHolder, t + ".name")
            ));
            meta.addItemFlags(
                    ItemFlag.HIDE_ADDITIONAL_TOOLTIP
            );
            meta.setLore(List.of(LegacyComponentSerializer.legacySection().serialize(Message.msg(uiHolder, t + ".lore")).split("\n")));
        });

        return new GuiItem(
                displayBase,
                UIUtil.noInteract
        );
    }
}
