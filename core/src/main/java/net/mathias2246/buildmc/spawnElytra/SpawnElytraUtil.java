package net.mathias2246.buildmc.spawnElytra;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Objects;

public class SpawnElytraUtil {
    public static final @NotNull NamespacedKey USES_SPAWN_ELYTRA_KEY = Objects.requireNonNull(NamespacedKey.fromString("buildmc:uses_spawn_elytra"));
    public static final @NotNull NamespacedKey USES_SPAWN_ELYTRA_BOOST_KEY = Objects.requireNonNull(NamespacedKey.fromString("buildmc:uses_spawn_elytra_boost"));

    public static final EnumSet<Material> BLOCK_EXCEPTIONS = EnumSet.of(
            Material.AIR,
            Material.CAVE_AIR,
            Material.VOID_AIR,
            Material.STRUCTURE_VOID,
            Material.SHORT_GRASS,
            Material.SHORT_DRY_GRASS,
            Material.TALL_GRASS,
            Material.TALL_DRY_GRASS,
            Material.BUSH,
            Material.DEAD_BUSH,
            Material.LEAF_LITTER,
            Material.PINK_PETALS,
            Material.WILDFLOWERS,
            Material.FERN,
            Material.LARGE_FERN,
            Material.DANDELION,
            Material.POPPY,
            Material.BLUE_ORCHID,
            Material.ALLIUM,
            Material.AZURE_BLUET,
            Material.RED_TULIP,
            Material.ORANGE_TULIP,
            Material.WHITE_TULIP,
            Material.PINK_TULIP,
            Material.OXEYE_DAISY,
            Material.CORNFLOWER,
            Material.LILY_OF_THE_VALLEY,
            Material.TORCHFLOWER,
            Material.CLOSED_EYEBLOSSOM,
            Material.OPEN_EYEBLOSSOM,
            Material.LILAC,
            Material.PEONY,
            Material.GLOW_LICHEN,
            Material.SUNFLOWER,
            Material.PITCHER_PLANT,

            Material.WARPED_ROOTS,
            Material.NETHER_SPROUTS,
            Material.CRIMSON_ROOTS,

            Material.SCULK_VEIN,

            Material.HANGING_ROOTS,

            Material.OAK_SAPLING,
            Material.SPRUCE_SAPLING,
            Material.BIRCH_SAPLING,
            Material.JUNGLE_SAPLING,
            Material.ACACIA_SAPLING,
            Material.DARK_OAK_SAPLING,
            Material.MANGROVE_PROPAGULE,
            Material.CHERRY_SAPLING,
            Material.PALE_OAK_SAPLING,

            Material.RAIL,
            Material.ACTIVATOR_RAIL,
            Material.DETECTOR_RAIL,
            Material.POWERED_RAIL,

            Material.OAK_PRESSURE_PLATE,
            Material.SPRUCE_PRESSURE_PLATE,
            Material.BIRCH_PRESSURE_PLATE,
            Material.ACACIA_PRESSURE_PLATE,
            Material.JUNGLE_PRESSURE_PLATE,
            Material.DARK_OAK_PRESSURE_PLATE,
            Material.CRIMSON_PRESSURE_PLATE,
            Material.CHERRY_PRESSURE_PLATE,
            Material.PALE_OAK_PRESSURE_PLATE,
            Material.MANGROVE_PRESSURE_PLATE,
            Material.BAMBOO_PRESSURE_PLATE,
            Material.WARPED_PRESSURE_PLATE,
            Material.STONE_PRESSURE_PLATE,
            Material.POLISHED_BLACKSTONE_PRESSURE_PLATE,
            Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
            Material.LIGHT_WEIGHTED_PRESSURE_PLATE,

            Material.OAK_BUTTON,
            Material.SPRUCE_BUTTON,
            Material.BIRCH_BUTTON,
            Material.ACACIA_BUTTON,
            Material.JUNGLE_BUTTON,
            Material.DARK_OAK_BUTTON,
            Material.CRIMSON_BUTTON,
            Material.CHERRY_BUTTON,
            Material.PALE_OAK_BUTTON,
            Material.MANGROVE_BUTTON,
            Material.BAMBOO_BUTTON,
            Material.WARPED_BUTTON,
            Material.STONE_BUTTON,
            Material.POLISHED_BLACKSTONE_BUTTON,

            Material.LEVER,

            Material.REDSTONE,

            Material.REDSTONE_TORCH,
            Material.REDSTONE_WALL_TORCH,
            Material.TORCH,
            Material.WALL_TORCH,
            Material.COPPER_TORCH,
            Material.COPPER_WALL_TORCH,

            Material.TRIPWIRE,
            Material.TRIPWIRE_HOOK,
            Material.STRING,

            Material.PALE_HANGING_MOSS,

            Material.BROWN_MUSHROOM,
            Material.RED_MUSHROOM,
            Material.CRIMSON_FUNGUS,
            Material.WARPED_FUNGUS,

            Material.PAINTING,
            Material.ITEM_FRAME,
            Material.GLOW_ITEM_FRAME,

            Material.WHITE_BANNER,
            Material.LIGHT_GRAY_BANNER,
            Material.GRAY_BANNER,
            Material.BLACK_BANNER,
            Material.BROWN_BANNER,
            Material.RED_BANNER,
            Material.ORANGE_BANNER,
            Material.YELLOW_BANNER,
            Material.LIME_BANNER,
            Material.GREEN_BANNER,
            Material.CYAN_BANNER,
            Material.LIGHT_BLUE_BANNER,
            Material.BLUE_BANNER,
            Material.PURPLE_BANNER,
            Material.MAGENTA_BANNER,
            Material.PINK_BANNER
    );

    public static final boolean[] BLOCK_EXCEPTION_LOOKUP = new boolean[Material.values().length];

    static {
        for (Material material : BLOCK_EXCEPTIONS) {
            BLOCK_EXCEPTION_LOOKUP[material.ordinal()] = true;
        }
    }

    /**Checks if the given Player uses the spawn elytra.
     * @return True if, the player has the uses_spawn_elytra metadata*/
    public static boolean isUsingSpawnElytra(@NotNull Player player) {
        return player.getPersistentDataContainer().has(USES_SPAWN_ELYTRA_KEY);
    }

    /**Checks if the given Player uses the spawn elytra.
     * @return True if, the player has the uses_spawn_elytra metadata*/
    public static boolean isUsingSpawnElytra(@NotNull Entity entity) {
        return entity.getPersistentDataContainer().has(USES_SPAWN_ELYTRA_KEY);
    }

    /**Checks if the given Player uses the spawn elytra boost.
     * @return True if, the player has the uses_spawn_elytra_boost metadata*/
    public static boolean isPlayerBoosted(@NotNull Player player) {
        return player.getPersistentDataContainer().has(USES_SPAWN_ELYTRA_BOOST_KEY);
    }

    /**Checks if the given Player uses the spawn elytra boost.
     * @return True if, the player has the uses_spawn_elytra_boost metadata*/
    public static boolean isPlayerBoosted(@NotNull Entity entity) {
        return entity.getPersistentDataContainer().has(USES_SPAWN_ELYTRA_BOOST_KEY);
    }

    public static void resetBoost(@NotNull Player player) {
        player.getPersistentDataContainer().remove(USES_SPAWN_ELYTRA_BOOST_KEY);
    }

    public static void resetBoost(@NotNull Entity entity) {
        entity.getPersistentDataContainer().remove(USES_SPAWN_ELYTRA_BOOST_KEY);
    }

    /**Checks if the player is in survival or adventure mode.*/
    public static boolean isSurvival(@NotNull Player player) {
        return isSurvival(player.getGameMode());
    }

    /**Checks if the gamemode is survival or adventure mode.*/
    public static boolean isSurvival(@NotNull GameMode gameMode) {
        return gameMode == GameMode.SURVIVAL || gameMode == GameMode.ADVENTURE;
    }

    /**Stops the player from flying.
     * Removes all spawn-elytra related metadata and resets all flight related attributes.*/
    public static void stopFlying(@NotNull Player player) {
        player.getPersistentDataContainer().remove(USES_SPAWN_ELYTRA_BOOST_KEY);
        player.getPersistentDataContainer().remove(USES_SPAWN_ELYTRA_KEY);
        player.setFallDistance(0);
        if (isSurvival(player)) player.setAllowFlight(false);
        player.setFlying(false);
        player.setGliding(false);

    }

    /**Makes the player glide using the spawn-elytra.*/
    public static void setPlayerFlying(@NotNull Player player) {
        var mode = player.getGameMode();
        if (!isSurvival(player)) return;
        player.setGliding(true);
        player.setAllowFlight(false);
        player.setFlying(false);

        player.getPersistentDataContainer().set(USES_SPAWN_ELYTRA_KEY, PersistentDataType.BOOLEAN, true);
    }

    public static void applyBoost(@NotNull Player player, double multiplier, double verticalVelocity) {
        player.getPersistentDataContainer().set(USES_SPAWN_ELYTRA_BOOST_KEY, PersistentDataType.BOOLEAN, true);
        applyRawBoost(player, multiplier, verticalVelocity);
    }

    public static void applyRawBoost(@NotNull Player player, double multiplier, double verticalVelocity) {
        player.setVelocity(player.getLocation().getDirection().multiply(multiplier).setY(verticalVelocity));
    }
}
