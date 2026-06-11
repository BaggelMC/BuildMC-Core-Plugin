package net.mathias2246.buildmc.spawnElytra;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.util.AudienceUtil;
import net.mathias2246.buildmc.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DisableRiptideListener extends PacketAdapter implements Listener {

    private final Set<UUID> chargingRiptide = new HashSet<>();

    public DisableRiptideListener(Plugin plugin, ProtocolManager protocolManager) {
        super(plugin, ListenerPriority.HIGHEST,
                PacketType.Play.Client.USE_ITEM,
                PacketType.Play.Client.BLOCK_PLACE,
                PacketType.Play.Client.BLOCK_DIG);

        protocolManager.addPacketListener(this);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        Player player = event.getPlayer();
        PacketType type = event.getPacketType();

        // Clean up tracking on release regardless
        if (type == PacketType.Play.Client.BLOCK_DIG) {
            EnumWrappers.PlayerDigType digType = event.getPacket()
                    .getPlayerDigTypes().read(0);
            if (digType == EnumWrappers.PlayerDigType.RELEASE_USE_ITEM) {
                chargingRiptide.remove(player.getUniqueId());
            }
            return;
        }

        // USE_ITEM / BLOCK_PLACE
        if (!SpawnElytraUtil.isUsingSpawnElytra(player)) return;
        if (!holdsRiptideTrident(player)) return;
        if (!player.isInWater() && !isInRain(player)) return;

        event.setCancelled(true);
        chargingRiptide.add(player.getUniqueId());

        // Yoink the trident for 2 ticks
        PlayerInventory inv = player.getInventory();
        boolean inMainHand = hasRiptide(inv.getItemInMainHand());
        ItemStack trident = inMainHand ? inv.getItemInMainHand() : inv.getItemInOffHand();

        if (inMainHand) {
            inv.setItemInMainHand(null);
        } else {
            inv.setItemInOffHand(null);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (inMainHand) {
                inv.setItemInMainHand(trident);
            } else {
                inv.setItemInOffHand(trident);
            }
            chargingRiptide.remove(player.getUniqueId());
        }, 2L);

        Component text = Message.msg(player, "messages.spawn-elytra.riptide-disabled-hint");
        AudienceUtil.sendActionBar(player, text);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        chargingRiptide.remove(event.getPlayer().getUniqueId());
    }

    public void clearCharging(Player player) {
        chargingRiptide.remove(player.getUniqueId());
    }

    private boolean holdsRiptideTrident(Player player) {
        return hasRiptide(player.getInventory().getItemInMainHand())
                || hasRiptide(player.getInventory().getItemInOffHand());
    }

    private boolean hasRiptide(ItemStack item) {
        return item != null
                && item.getType() == Material.TRIDENT
                && item.containsEnchantment(Enchantment.RIPTIDE);
    }

    private boolean isInRain(Player player) {
        if (!player.getWorld().hasStorm()) return false;
        return player.getLocation().getBlockY()
                >= player.getWorld().getHighestBlockYAt(player.getLocation());
    }
}