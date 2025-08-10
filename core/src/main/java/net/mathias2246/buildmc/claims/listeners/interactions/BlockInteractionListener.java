package net.mathias2246.buildmc.claims.listeners.interactions;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.tags.MaterialTag;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Objects;
import java.util.Set;

public class BlockInteractionListener implements Listener {

    public static MaterialTag interactionBlocks = new MaterialTag(

            Objects.requireNonNull(NamespacedKey.fromString("buildmc:interaction/blocks")),
            Set.of(
                    Material.COMPARATOR,
                    Material.REPEATER,
                    Material.LEVER,
                    Material.BAMBOO_BUTTON,
                    Material.BIRCH_BUTTON,
                    Material.ACACIA_BUTTON,
                    Material.CHERRY_BUTTON,
                    Material.JUNGLE_BUTTON,
                    Material.CRIMSON_BUTTON,
                    Material.DARK_OAK_BUTTON,
                    Material.MANGROVE_BUTTON,
                    Material.OAK_BUTTON,
                    Material.PALE_OAK_BUTTON,
                    Material.POLISHED_BLACKSTONE_BUTTON,
                    Material.SPRUCE_BUTTON,
                    Material.STONE_BUTTON,
                    Material.WARPED_BUTTON,
                    Material.CAULDRON,
                    Material.LAVA_CAULDRON,
                    Material.POWDER_SNOW_CAULDRON,
                    Material.BEACON,
                    Material.BEEHIVE,
                    Material.BEE_NEST,
                    Material.RESPAWN_ANCHOR,
                    Material.JUKEBOX,
                    Material.CAMPFIRE,
                    Material.SOUL_CAMPFIRE,
                    Material.DECORATED_POT,
                    Material.DAYLIGHT_DETECTOR,
                    Material.NOTE_BLOCK,
                    Material.CHISELED_BOOKSHELF,
                    Material.FLOWER_POT,
                    Material.SPAWNER,
                    Material.TNT,
                    Material.CANDLE,
                    Material.BLACK_CANDLE,
                    Material.WHITE_CANDLE,
                    Material.CYAN_CANDLE,
                    Material.BLUE_CANDLE,
                    Material.BROWN_CANDLE,
                    Material.GRAY_CANDLE,
                    Material.GREEN_CANDLE,
                    Material.LIGHT_BLUE_CANDLE,
                    Material.LIGHT_GRAY_CANDLE,
                    Material.LIME_CANDLE,
                    Material.MAGENTA_CANDLE,
                    Material.ORANGE_CANDLE,
                    Material.PINK_CANDLE,
                    Material.PURPLE_CANDLE,
                    Material.RED_CANDLE,
                    Material.YELLOW_CANDLE
            )
    );

    @EventHandler
    public void onBlockInteraction(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        Block block = event.getClickedBlock();
        if (!block.getType().isInteractable()) return;

        if (!interactionBlocks.isTagged(block.getType())) return;

        Player player = event.getPlayer();
        if (!ClaimManager.isPlayerAllowed(CoreMain.claimManager, player, block.getLocation())) {
            CoreMain.mainClass.sendPlayerActionBar(player, Component.translatable("messages.claims.not-accessible.interact"));
            event.setCancelled(true);
        }

    }

}
