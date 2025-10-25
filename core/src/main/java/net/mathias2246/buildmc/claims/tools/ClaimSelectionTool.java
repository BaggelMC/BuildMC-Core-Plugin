package net.mathias2246.buildmc.claims.tools;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.item.abstractTypes.AbstractSelectionTool;
import net.mathias2246.buildmc.util.Message;
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
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"PatternValidation", "UnstableApiUsage"})
public class ClaimSelectionTool extends AbstractSelectionTool {

    public final @NotNull Sound successSound;
    public final @NotNull Sound mistakeSound;
    public final @NotNull ParticleSpawner.Builder<?> particles;

    @ApiStatus.Internal
    public ClaimSelectionTool(@NotNull Plugin plugin, @NotNull NamespacedKey key, @NotNull ParticleSpawner.Builder<?> particles) {
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
        this.particles = particles;
        maxSelectionSize = plugin.getConfig().getInt("claims.tool.limit-selection", 8);
    }

    @Override
    public boolean canUse(@NotNull ItemStack item, @NotNull PlayerInteractEvent event) {
        return !event.getPlayer().hasCooldown(item);
    }

    private final int maxSelectionSize;

    @Override
    protected @NotNull ItemStack buildDefaultItemStack() {
        var claimToolItem = Material.getMaterial(getPlugin().getConfig().getString("claims.tool.tool-item", "carrot_on_a_stick").toUpperCase());
        if (claimToolItem == null) claimToolItem = Material.CARROT_ON_A_STICK;

        var claimToolItemstack = new ItemStack(claimToolItem);


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

    public boolean isSelectionToLarge(@NotNull Location from, @NotNull Location to, @NotNull Player player) {
        if (maxSelectionSize < 0) return false;

        var fx = from.getChunk().getX();
        var fz = from.getChunk().getZ();
        var tx = to.getChunk().getX();
        var tz = to.getChunk().getZ();

        var startX = Math.min(fx, tx);
        var startZ = Math.min(fz, tz);
        var endX = Math.max(fx, tx);
        var endZ = Math.max(fz, tz);
        return (endX - startX > maxSelectionSize) || (endZ - startZ > maxSelectionSize);
    }

    @Override
    public boolean allowSecondSelection(@NotNull ItemStack item, @NotNull Location at, @NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = player.getLocation().getChunk();

        var first = getFirstSelection(player);

        if (player.hasCooldown(item)) {
            CoreMain.mainClass.sendPlayerMessage(player, Component.translatable("messages.claims.tool.tool-cooldown"));
            CoreMain.soundManager.playSound(player, mistakeSound);
            return false;
        } else if (first == null) {
            CoreMain.mainClass.sendPlayerMessage(player, Component.translatable("messages.claims.tool.no-first-selection"));
            CoreMain.soundManager.playSound(player, mistakeSound);

            return false;
        } else if (isSelectionToLarge(first, at, player)) {
            var msg = Message.msg(player, "messages.claims.tool.selection-too-large");
            msg = msg.replaceText(
                    TextReplacementConfig.builder().matchLiteral("%selection_limit%").replacement(Integer.toString(maxSelectionSize)).build()
            );

            CoreMain.mainClass.sendPlayerMessage(player, msg);
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

        player.setCooldown(item, 60);

        if (!player.hasMetadata("claim_selection_particles")) {
            player.setMetadata("claim_selection_particles", new FixedMetadataValue(getPlugin(), null));
            var p = particles.build(player);
            p.runTaskTimer(getPlugin(), 0, p.delay);
        }
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
