package net.mathias2246.buildmc.claims.tools;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.item.abstractTypes.AbstractSelectionTool;
import net.mathias2246.buildmc.util.ParticleSpawner;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("PatternValidation")
public class ClaimSelectionTool extends AbstractSelectionTool {

    public final @NotNull Sound successSound;
    public final @NotNull Sound mistakeSound;

    public ClaimSelectionTool(@NotNull Plugin plugin, @NotNull NamespacedKey key) {
        super(plugin, key);
        successSound = net.kyori.adventure.sound.Sound.sound(
                Key.key(CoreMain.plugin.getConfig().getString("sounds.success", "minecraft:block.note_block.bell")),
                net.kyori.adventure.sound.Sound.Source.MASTER,
                1f,
                1f
        );
        mistakeSound = Sound.sound(

                Key.key(CoreMain.plugin.getConfig().getString("sounds.mistake", "minecraft:block.note_block.snare")),
                Sound.Source.MASTER,
                1f,
                1f
        );
    }

    @Override
    public boolean canUse(@NotNull ItemStack item, @NotNull PlayerInteractEvent event) {
        return !event.getPlayer().hasCooldown(item);
    }

    private int maxSelectionSize = -1;

    @Override
    protected @NotNull ItemStack buildDefaultItemStack() {
        var claimToolItem = Material.getMaterial(getPlugin().getConfig().getString("claims.tool.tool-item", "carrot_on_a_stick").toUpperCase());
        if (claimToolItem == null) claimToolItem = Material.CARROT_ON_A_STICK;

        var claimToolItemstack = new ItemStack(claimToolItem);

        maxSelectionSize = getPlugin().getConfig().getInt("claims.tool.limit-selection", 8);
        if (maxSelectionSize < 0) maxSelectionSize = Integer.MAX_VALUE;

        ItemMeta m = claimToolItemstack.getItemMeta();

        if (m != null) {
            m.setItemName("Select Claim Corners");
            m.setTool(null);
            m.addItemFlags(
                    ItemFlag.HIDE_ATTRIBUTES,
                    ItemFlag.HIDE_UNBREAKABLE
            );
            m.setUnbreakable(true);
            m.setRarity(ItemRarity.UNCOMMON);
            m.setEnchantmentGlintOverride(true);
            m.setEnchantable(null);
            claimToolItemstack.setItemMeta(m);
        }

        return claimToolItemstack;
    }

    @Override
    public boolean allowFirstSelection(@NotNull ItemStack item, @NotNull Location at, @NotNull PlayerInteractEvent event) {
        return true;
    }

    @Override
    public boolean allowSecondSelection(@NotNull ItemStack item, @NotNull Location at, @NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = player.getLocation().getChunk();
        if (player.hasCooldown(item)) {
            CoreMain.mainClass.sendPlayerMessage(player, Component.translatable("messages.claims.tool.tool-cooldown"));
            CoreMain.soundManager.playSound(player, mistakeSound);
            return false;
        }
        return true;
    }

    @Override
    public void onSuccessfulFirstSelection(@NotNull ItemStack item, @NotNull Location at, @NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        CoreMain.mainClass.sendPlayerMessage(player, Component.translatable("messages.claims.tool.set-pos1"));

        CoreMain.soundManager.playSound(player, successSound);
    }

    @Override
    public void onSuccessfulSecondSelection(@NotNull ItemStack item, @NotNull Location at, @NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();

        player.setCooldown(item, 60);

        CoreMain.mainClass.sendPlayerMessage(player, Component.translatable("messages.claims.tool.set-pos2"));
        CoreMain.soundManager.playSound(player, successSound);

        if (player.hasMetadata(firstSelectionKey) && player.hasMetadata(secondSelectionKey)) {
            CoreMain.mainClass.sendPlayerMessage(player, Component.translatable("messages.claims.tool.both-positions-set"));
        }
    }
}
