package net.mathias2246.buildmc.claims.tools;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.item.abstractTypes.AbstractSelectionTool;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.util.Message;
import net.mathias2246.buildmc.util.ParticleSpawner;
import net.mathias2246.buildmc.util.SoundUtil;
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

import java.util.Objects;

@SuppressWarnings({"UnstableApiUsage"})
public class ClaimSelectionTool extends AbstractSelectionTool {


    public final @NotNull ParticleSpawner.Builder<?> particles;

    @ApiStatus.Internal
    public ClaimSelectionTool(@NotNull Plugin plugin, @NotNull NamespacedKey key, @NotNull ParticleSpawner.Builder<?> particles) {
        super(plugin, key);

        this.particles = particles;
    }

    @Override
    public boolean canUse(@NotNull ItemStack item, @NotNull PlayerInteractEvent event) {
        return !event.getPlayer().hasCooldown(item);
    }

    public static final int maxSelectionSize = CoreMain.plugin.getConfig().getInt("claims.tool.limit-selection", 8);


    @Override
    protected @NotNull ItemStack buildDefaultItemStack() {

        ItemStack claimToolItemstack = new ItemStack(
                Material.valueOf(getPlugin().getConfig().getString("claims.tool.tool-item-material", "CARROT_ON_A_STICK").toUpperCase()));

        ItemMeta m = claimToolItemstack.getItemMeta();

        if (m != null) {
            m.itemName(Component.text("Select Claim Corners"));
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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isSelectionInSameWorld(@NotNull Location from, @NotNull Location to) {
        return Objects.equals(from.getWorld(), to.getWorld());
    }

    @Override
    public boolean allowFirstSelection(@NotNull ItemStack item, @NotNull Location at, @NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();

        var second = getSecondSelection(player);

        if (at.getWorld() == null) return false;

        if (!ClaimManager.isWorldAllowed(at.getWorld())) {
            CoreMain.plugin.sendMessage(player, Component.translatable("messages.claims.create.world-not-allowed"));
            CoreMain.soundManager.playSound(player, SoundUtil.notification);
            return false;
        }

        if (second != null) {
            if (!isSelectionInSameWorld(at, second)) {
                CoreMain.plugin.sendMessage(player, Component.translatable("messages.claims.create.different-worlds"));
                CoreMain.soundManager.playSound(player, SoundUtil.notification);
                return false;
            }

        }

        return true;
    }

    public static boolean isSelectionToLarge(@NotNull Location from, @NotNull Location to, @NotNull Player player) {
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

        if (at.getWorld() == null) return false;

        if (!ClaimManager.isWorldAllowed(at.getWorld())) {
            CoreMain.plugin.sendMessage(player, Component.translatable("messages.claims.create.world-not-allowed"));
            CoreMain.soundManager.playSound(player, SoundUtil.notification);
            return false;
        }

        var first = getFirstSelection(player);

        if (player.hasCooldown(item)) {
            CoreMain.plugin.sendMessage(player, Component.translatable("messages.claims.tool.tool-cooldown"));
            CoreMain.soundManager.playSound(player, SoundUtil.mistake);
            return false;
        } else if (first == null) {
            return true;
        } else if (!isSelectionInSameWorld(at, first)) {
            CoreMain.plugin.sendMessage(player, Component.translatable("messages.claims.create.different-worlds"));
            CoreMain.soundManager.playSound(player, SoundUtil.notification);
            return false;
        } else if (isSelectionToLarge(first, at, player)) {
            var msg = Message.msg(player, "messages.claims.tool.selection-too-large");
            msg = msg.replaceText(
                    TextReplacementConfig.builder().matchLiteral("%selection_limit%").replacement(Integer.toString(maxSelectionSize)).build()
            );

            CoreMain.plugin.sendMessage(player, msg);
            CoreMain.soundManager.playSound(player, SoundUtil.mistake);
            return false;
        }

        return true;
    }

    @Override
    public void onSuccessfulFirstSelection(@NotNull ItemStack item, @NotNull Location at, @NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        CoreMain.plugin.sendMessage(player, Component.translatable("messages.claims.tool.set-pos1"));

        CoreMain.soundManager.playSound(player, SoundUtil.success);

        player.setCooldown(item, 60);

        if (!player.hasMetadata("claim_selection_particles")) {
            player.setMetadata("claim_selection_particles", new FixedMetadataValue(getPlugin(), null));
            var p = particles.build(player);
            p.run();
        }
    }

    @Override
    public void onSuccessfulSecondSelection(@NotNull ItemStack item, @NotNull Location at, @NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();

        player.setCooldown(item, 60);

        CoreMain.plugin.sendMessage(player, Component.translatable("messages.claims.tool.set-pos2"));
        CoreMain.soundManager.playSound(player, SoundUtil.success);

        if (player.hasMetadata(firstSelectionKey) && player.hasMetadata(secondSelectionKey)) {
            CoreMain.plugin.sendMessage(player, Component.translatable("messages.claims.tool.both-positions-set"));
        }
    }
}
