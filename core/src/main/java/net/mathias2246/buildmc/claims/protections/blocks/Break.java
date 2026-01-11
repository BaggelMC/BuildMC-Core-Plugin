package net.mathias2246.buildmc.claims.protections.blocks;

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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Break extends Protection {

    public Break(@Nullable ConfigurationSection section) {
        //noinspection SimplifiableConditionalExpression
        super(Objects.requireNonNull(NamespacedKey.fromString("buildmc:block_breaking")), (section != null ? section.getBoolean("default", true) : true), section != null && section.getBoolean("is-hidden", false));
    }

    @Override
    public String getTranslationBaseKey() {
        return "claims.flags.player-break";
    }

    @Override
    public @NotNull GuiItem getDisplay(@NotNull Player uiHolder, @NotNull Gui gui) {
        String t = getTranslationBaseKey();

        return ProtectionUtil.createDisplayItem(uiHolder, Material.CAMPFIRE, t);
    }

    @EventHandler
    public void onPlayerMine(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (!ClaimManager.isPlayerAllowed(player, getKey(), event.getBlock().getLocation())) {
            CoreMain.plugin.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.block-break"));
            event.setCancelled(true);
        }
    }
}
