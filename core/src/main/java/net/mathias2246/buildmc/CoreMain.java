package net.mathias2246.buildmc;

import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.claims.listeners.*;
import net.mathias2246.buildmc.claims.listeners.interactions.BlockInteractionListener;
import net.mathias2246.buildmc.claims.listeners.interactions.ClaimFarmlandInteractionListener;
import net.mathias2246.buildmc.claims.listeners.interactions.DoorInteractionListener;
import net.mathias2246.buildmc.tags.MaterialTag;
import net.mathias2246.buildmc.util.SoundManager;
import net.mathias2246.buildmc.util.language.LanguageManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class CoreMain {

    public static Plugin plugin;

    public static MainClass mainClass;

    public static SoundManager soundManager;

    public static ClaimManager claimManager;

    private static FileConfiguration config;

    public static void initialize(@NotNull Plugin plugin) {
        CoreMain.plugin = plugin;

        config = plugin.getConfig();

        CoreMain.mainClass = (MainClass) plugin;

        initializeConfigs();

        LanguageManager.init();

        setupClaims();

    }

    public static void stop() {

    }

    private static void initializeConfigs() {

    }

    private static void setupClaims() {
        if (!config.getBoolean("claims.enabled", true)) return;
        claimManager = new ClaimManager(plugin, "claim-data.yml");

        if (config.getBoolean("claims.protections.containers", true)) {
            plugin.getServer().getPluginManager().registerEvents(new ClaimContainerListener(), plugin);
        }

        if (config.getBoolean("claims.protections.damage.explosion-block-damage", true) || config.getBoolean("claims.protections.explosion-entity-damage", true)) {
            plugin.getServer().getPluginManager().registerEvents(new ClaimExplosionsListener(), plugin);
        }
        if (config.getBoolean("claims.protections.damage.explosion-entity-damage", true)) {
            plugin.getServer().getPluginManager().registerEvents(new ClaimExplosionDamageListener(), plugin);
        }

        if (config.getBoolean("claims.protections.player-break", true)) {
            plugin.getServer().getPluginManager().registerEvents(new ClaimBreakListener(), plugin);
        }

        if (config.getBoolean("claims.protections.player-place", true)) {
            plugin.getServer().getPluginManager().registerEvents(new ClaimPlaceListener(), plugin);
        }

        if (config.getBoolean("claims.protections.damage.entity-damage", true)) {
            plugin.getServer().getPluginManager().registerEvents(new ClaimDamageProtectionListener(), plugin);
        }

        if (config.getBoolean("claims.protections.sign-editing", true)) {
            plugin.getServer().getPluginManager().registerEvents(new ClaimSignEditListener(), plugin);
        }

        if (config.getBoolean("claims.protections.end-crystal-place", true)) {
            plugin.getServer().getPluginManager().registerEvents(new ClaimEndCrystalPlaceEvent(), plugin);
        }
        if (config.getBoolean("claims.protections.entity-tame", true)) {
            plugin.getServer().getPluginManager().registerEvents(new ClaimEntityTameListener(), plugin);
        }

        if (config.getBoolean("claims.protections.prevent-interactions", true)) {
            plugin.getServer().getPluginManager().registerEvents(new ClaimFarmlandInteractionListener(), plugin);
            if (config.getBoolean("claims.protections.interactions.doors", true))
                plugin.getServer().getPluginManager().registerEvents(new DoorInteractionListener(), plugin);
            if (config.contains("claims.protections.interactions.blocks")) {
                List<String> list = config.getStringList("claims.protections.interactions.blocks");

                List<Material> m = new ArrayList<>();
                for (var v : list) {
                    m.add(Material.valueOf(v.toUpperCase()));
                }
                BlockInteractionListener.interactionBlocks = new MaterialTag(
                        Objects.requireNonNull(NamespacedKey.fromString("buildmc:interaction/blocks")),
                        Set.copyOf(m)
                );
                plugin.getServer().getPluginManager().registerEvents(new BlockInteractionListener(), plugin);
            }
            if (config.getBoolean("claims.protections.interactions.nametags", true))
                plugin.getServer().getPluginManager().registerEvents(new ClaimNameTagUseListener(), plugin);
            if (config.getBoolean("claims.protections.interactions.item-frames", true))
                plugin.getServer().getPluginManager().registerEvents(new ClaimItemFrameListener(), plugin);
            if (config.getBoolean("claims.protections.interactions.bone-meal", true))
                plugin.getServer().getPluginManager().registerEvents(new ClaimBonemealInteractListener(), plugin);
            if (config.getBoolean("claims.protections.interactions.paintings", true))
                plugin.getServer().getPluginManager().registerEvents(new ClaimPaintingInteractListener(), plugin);
            if (config.getBoolean("claims.protections.interactions.attach-leash", true))
                plugin.getServer().getPluginManager().registerEvents(new ClaimEntityLeashListener(), plugin);
            if (config.getBoolean("claims.protections.interactions.armor-stands", true))
                plugin.getServer().getPluginManager().registerEvents(new ChangeArmorStandListener(), plugin);
        }

        if (config.getBoolean("claims.protections.splash-potions", true)) {
            plugin.getServer().getPluginManager().registerEvents(new ClaimPotionSplashListener(), plugin);
        }

        if (config.getBoolean("claims.protections.vehicle-enter", true)) {
            plugin.getServer().getPluginManager().registerEvents(new ClaimVehicleEnterListener(), plugin);
        }

        if (config.getBoolean("claims.protections.bucket-usage", true)) {
            plugin.getServer().getPluginManager().registerEvents(new ClaimBucketUseListener(), plugin);
        }

        if (config.getBoolean("claims.protections.prevent-entity-modifications", true)) {
            plugin.getServer().getPluginManager().registerEvents(new ClaimEntityChangeBlockListener(
                    config.getBoolean("claims.protections.entity-modifications.wither", true),
                    config.getBoolean("claims.protections.entity-modifications.enderman", false),
                    config.getBoolean("claims.protections.entity-modifications.ravager", false)

            ), plugin);
        }

        if (config.getBoolean("claims.protections.item-pickup", false)) {
            plugin.getServer().getPluginManager().registerEvents(new ClaimItemPickupListener(), plugin);
        }

        if (config.getBoolean("claims.protections.item-drop", false)) {
            plugin.getServer().getPluginManager().registerEvents(new ClaimItemDropListener(), plugin);
        }

        if (config.getBoolean("claims.protections.frostwalker", true)) {
            plugin.getServer().getPluginManager().registerEvents(new ClaimFrostWalkerListener(), plugin);
        }

        if (config.getBoolean("claims.protections.piston-movement-across-claim-borders", true)) {
            plugin.getServer().getPluginManager().registerEvents(new ClaimPistonMovementListener(), plugin);
        }
    }
}
