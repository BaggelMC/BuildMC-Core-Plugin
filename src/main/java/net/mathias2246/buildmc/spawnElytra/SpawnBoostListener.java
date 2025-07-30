package net.mathias2246.buildmc.spawnElytra;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.util.Message;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.KeybindComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static net.mathias2246.buildmc.Main.audiences;

public class SpawnBoostListener extends BukkitRunnable implements Listener {

    private final Plugin plugin;
    private final FileConfiguration config;
    private final ElytraZoneManager zoneManager;
    private final int multiplyValue;
    private final boolean boostEnabled;
    private final List<Player> flying = new ArrayList<>();
    private final List<Player> boosted = new ArrayList<>();

    public SpawnBoostListener(Plugin plugin, ElytraZoneManager zoneManager) {
        this.plugin = plugin;
        this.zoneManager = zoneManager;

        this.config = plugin.getConfig();
        this.multiplyValue = config.getInt("spawn-elytra.strength", 2);
        this.boostEnabled = config.getBoolean("spawn-elytra.enabled", true);

        this.runTaskTimer(this.plugin, 0, 3);
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) return;

            if (flying.contains(player) && !player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().isAir()) {
                player.setAllowFlight(false);
                player.setGliding(false);
                boosted.remove(player);
                flying.remove(player);
            }

            if (!player.isGliding()) {
                boolean inZone = zoneManager.isInZone(player);
                player.setAllowFlight(inZone);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!config.getBoolean("spawn-elytra.on-join-elytra-check", true)) return;

        Player player = event.getPlayer();

        flying.remove(player);
        boosted.remove(player);

        player.setGliding(false);

        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            player.setAllowFlight(false);
        }

    }

    @EventHandler
    public void onDoubleJump(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) return;

        if (!zoneManager.isInZone(player)) return;

        event.setCancelled(true);

        if (player.isGliding()) return;

        player.setGliding(true);
        player.setAllowFlight(false);
        flying.add(player);

        String message = Message.msgStr(player, "messages.spawn-elytra.boost-hint");

        if (boostEnabled && !boosted.contains(player)) {
            String[] messageParts = message.split("%key%");
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new ComponentBuilder(messageParts[0])
                            .append(new KeybindComponent("key.swapOffhand"))
                            .append(messageParts.length > 1 ? messageParts[1] : "")
                            .create());
        }
    }

    @EventHandler
    public void onSwapItem(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (!boostEnabled || !flying.contains(player) || boosted.contains(player)) return;

        event.setCancelled(true);
        boosted.add(player);
        player.setVelocity(player.getLocation().getDirection().multiply(multiplyValue).setY(1.2)); // vertical boost
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) return;
        if (!(event.getEntity() instanceof Player player)) return;

        if (flying.contains(player) && (
                event.getCause() == EntityDamageEvent.DamageCause.FALL ||
                        event.getCause() == EntityDamageEvent.DamageCause.FLY_INTO_WALL)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onToggleGlide(EntityToggleGlideEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) return;
        if (!(event.getEntity() instanceof Player player)) return;

        if (flying.contains(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFireworkUse(PlayerInteractEvent event) {
        if (!config.getBoolean("spawn-elytra.disable-rockets")) return;

        Player player = event.getPlayer();

        if (!flying.contains(player)) return;

        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.FIREWORK_ROCKET) {
            event.setCancelled(true);

            Component text = Message.msg(player, "messages.spawn-elytra.firework-disabled-hint");
            audiences.player(player).sendActionBar(text);
        }
    }

    @EventHandler
    public void onEntityMount(EntityMountEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;
        if (flying.contains(player)) {
            player.setGliding(false);
            boosted.remove(player);
            flying.remove(player);
            player.setAllowFlight(false);
        }
    }

}
