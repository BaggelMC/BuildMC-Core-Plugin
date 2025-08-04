package net.mathias2246.buildmc.claims;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.Main;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import static net.mathias2246.buildmc.Main.claimManager;

public class ClaimInteractionListener implements Listener {

    private final boolean protectionLevers = Main.config.getBoolean("claims.protections.interactions.levers");
    private final boolean protectionButtons = Main.config.getBoolean("claims.protections.interactions.buttons");
    private final boolean protectionRepeaters = Main.config.getBoolean("claims.protections.interactions.repeaters");
    private final boolean protectionComparators = Main.config.getBoolean("claims.protections.interactions.comparators");

    private final boolean protectionPressurePlates = Main.config.getBoolean("claims.protections.interactions.pressure-plates");
    private final boolean protectionTrapdoors = Main.config.getBoolean("claims.protections.interactions.trapdoors");
    private final boolean protectionDoors = Main.config.getBoolean("claims.protections.interactions.doors");
    private final boolean protectionFenceGates = Main.config.getBoolean("claims.protections.interactions.fence-gates");
    private final boolean protectionFarmland = Main.config.getBoolean("claims.protections.interactions.farmland");

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        Block block = event.getClickedBlock();
        Material type = block.getType();
        Player player = event.getPlayer();

        // Check if it's an interactable block we want to restrict
        if (isRestrictedInteractable(type)) {
            if (!ClaimManager.isPlayerAllowed(claimManager, player, block.getLocation())) {
                event.setCancelled(true);
                player.sendActionBar(Component.translatable("messages.claims.not-accessible.interact"));
            }
        }
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (!protectionFarmland) return;

        if (!(event.getEntity() instanceof Player player)) return;

        if (event.getBlock().getType() == Material.FARMLAND) {
            if (!ClaimManager.isPlayerAllowed(claimManager, player, event.getBlock().getLocation())) {
                player.sendActionBar(Component.translatable("messages.claims.not-accessible.interact"));
                event.setCancelled(true);
            }
        }
    }

    // Not pretty and less performant, but we don't forget to add things
    private boolean isRestrictedInteractable(Material type) {
        String name = type.name();

        if (protectionLevers && name.equals("LEVER")) return true;
        else if (protectionButtons && name.contains("BUTTON")) return true;
        else if (protectionPressurePlates && name.contains("PRESSURE_PLATE")) return true;
        else if (protectionRepeaters && name.equals("REPEATER")) return true;
        else if (protectionComparators && name.equals("COMPARATOR")) return true;
        else if (protectionTrapdoors && name.contains("TRAPDOOR")) return true;
        else if (protectionDoors && name.endsWith("_DOOR")) return true;
        else if (protectionFenceGates && name.contains("FENCE_GATE")) return true;

        else return false;
    }

}
