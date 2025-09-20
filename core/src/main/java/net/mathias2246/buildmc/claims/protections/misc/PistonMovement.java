package net.mathias2246.buildmc.claims.protections.misc;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
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
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class PistonMovement extends Protection {
    public PistonMovement(@Nullable ConfigurationSection section) {
        super(Objects.requireNonNull(NamespacedKey.fromString("buildmc:piston_movement")), (section != null ? section.getBoolean("default", true) : true), section != null && section.getBoolean("is-hidden", false));
    }

    @Override
    public String getTranslationBaseKey() {
        return "claims.flags.piston-movement-across-claim-borders";
    }

    @Override
    public @NotNull GuiItem getDisplay(@NotNull Player uiHolder, @NotNull Gui gui) {
        String t = getTranslationBaseKey();

        ItemStack displayBase = new ItemStack(Material.PISTON);
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

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        try {
            Block piston = event.getBlock();
            Claim pistonClaim = ClaimManager.getClaim(piston.getChunk());
            BlockFace direction = event.getDirection();

            List<Block> movedBlocks = event.getBlocks();
            for (Block movedBlock : movedBlocks) {
                Block destinationBlock = movedBlock.getRelative(direction);

                Claim fromClaim = ClaimManager.getClaim(movedBlock.getChunk());
                Claim toClaim = ClaimManager.getClaim(destinationBlock.getChunk());

                if (pistonClaim != null && toClaim == null) {
                    continue;
                }

                if (!isPistonMoveAllowed(pistonClaim, fromClaim, toClaim)) {
                    event.setCancelled(true);
                    return;
                }
            }
        } catch (SQLException e) {
            CoreMain.plugin.getLogger().severe("An SQLException occurred in ClaimPistonMovementListener in onPistonExtend: " + e);
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (!event.isSticky()) return;

        try {
            Block piston = event.getBlock();
            Claim pistonClaim = ClaimManager.getClaim(piston.getChunk());
            BlockFace direction = event.getDirection();

            List<Block> movedBlocks = event.getBlocks();
            for (Block movedBlock : movedBlocks) {
                Block destinationBlock = movedBlock.getRelative(direction);

                Claim fromClaim = ClaimManager.getClaim(movedBlock.getChunk());
                Claim toClaim = ClaimManager.getClaim(destinationBlock.getChunk());

                if (pistonClaim != null && !requiresProtection(fromClaim)) {
                    continue;
                }

                // Optimization: Skip extra checks if staying in same chunk
                if (movedBlock.getChunk().equals(destinationBlock.getChunk())) {
                    if (!isPistonMoveAllowed(pistonClaim, fromClaim, fromClaim)) {
                        event.setCancelled(true);
                        return;
                    }
                    continue;
                }

                if (!isPistonMoveAllowed(pistonClaim, fromClaim, toClaim)) {
                    event.setCancelled(true);
                    return;
                }
            }
        } catch (SQLException e) {
            CoreMain.plugin.getLogger().severe("An SQLException occurred in ClaimPistonMovementListener in onPistonRetract: " + e);
            event.setCancelled(true);
        }
    }


    /**
     * Determines if piston movement between claims is allowed.
     * Takes into account ownership and claim flags.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isPistonMoveAllowed(Claim pistonClaim, Claim fromClaim, Claim toClaim) {
        String pistonOwner = pistonClaim != null ? pistonClaim.getOwnerId() : null;
        String fromOwner = fromClaim != null ? fromClaim.getOwnerId() : null;
        String toOwner = toClaim != null ? toClaim.getOwnerId() : null;

        // Check flags
        if (requiresProtection(pistonClaim) || requiresProtection(fromClaim) || requiresProtection(toClaim)) {
            if (!equalsOrNull(fromOwner, toOwner)) return false;
            if (!equalsOrNull(pistonOwner, toOwner)) return false;
            return equalsOrNull(pistonOwner, fromOwner);
        }

        return true;
    }

    private boolean requiresProtection(Claim claim) {
        return claim != null && claim.hasProtection(getKey())
;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean equalsOrNull(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
}
