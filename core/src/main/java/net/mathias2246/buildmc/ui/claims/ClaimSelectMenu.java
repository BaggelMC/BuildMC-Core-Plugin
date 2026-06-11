package net.mathias2246.buildmc.ui.claims;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.item.ItemUtil;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.ui.UIUtil;
import net.mathias2246.buildmc.util.AudienceUtil;
import net.mathias2246.buildmc.util.Message;
import net.mathias2246.buildmc.util.SoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static net.mathias2246.buildmc.CoreMain.plugin;
import static net.mathias2246.buildmc.ui.claims.ClaimEditMenu.BACKGROUND;

public class ClaimSelectMenu {

    public static final int SLOTS_PER_PAGE = 45;

    public static void open(@NotNull Player player) {

        boolean showAllClaims = player.hasPermission("buildmc.show-all-claims");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, task -> {

            List<Claim> claims = new ArrayList<>();
            List<Long> claimIds = new ArrayList<>();

                    if (showAllClaims) {
                        for (var playerClaims : ClaimManager.playerOwner.values()) {
                            claimIds.addAll(playerClaims);
                        }

                        for (var teamClaims : ClaimManager.teamOwner.values()) {
                            claimIds.addAll(teamClaims);
                        }
                    } else {
                        // Get claims
                         claimIds.addAll(ClaimManager.playerOwner
                                .getOrDefault(player.getUniqueId(), Collections.emptyList()));

                        claimIds.addAll(Optional.ofNullable(ClaimManager.getPlayerTeam(player))
                                .map(t -> ClaimManager.teamOwner.getOrDefault(t.getName(), Collections.emptyList()))
                                .orElse(Collections.emptyList()));
                    }

                        for (long id : claimIds) {
                            Claim c = ClaimManager.getClaimByID(id);
                            if (c != null) claims.add(c);
                        }

                    if (player.hasPermission("buildmc.admin") || showAllClaims) {
                        for (long id : ClaimManager.serverClaims) {
                            Claim c = ClaimManager.getClaimByID(id);
                            if (c != null) claims.add(c);
                        }

                        for (long id : ClaimManager.placeholderClaims) {
                            Claim c = ClaimManager.getClaimByID(id);
                            if (c != null) claims.add(c);
                        }
                    }

                    List<GuiItem> claimButtons = buildClaimItems(player, claims, showAllClaims);

                    ChestGui gui = new ChestGui(6, ComponentHolder.of(Message.msg(player, "messages.claims.ui.select-menu.title")));

                    PaginatedPane pages = new PaginatedPane(9, 5);

                    pages.populateWithGuiItems(claimButtons);

                    StaticPane background = new StaticPane(9, 5);
                    background.setPriority(Pane.Priority.LOWEST);
                    for (int x = 0; x < 9; x++) {
                        for (int y = 0; y < 5; y++) {
                            background.addItem(UIUtil.INVISIBLE_PANE, x, y);
                        }
                    }

                    gui.addPane(Slot.fromXY(0, 0), background);

                    StaticPane bottomBar = UIUtil.BOTTOM_BAR.copy();

                    bottomBar.addItem(UIUtil.EXIT_BUTTON, 8, 0);

                    ItemStack newClaimItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
                    ItemUtil.setName(newClaimItem, Message.msg(player, "messages.claims.ui.select-menu.new-claim-button"));
                    bottomBar.addItem(new GuiItem(newClaimItem, e -> {
                        e.setCancelled(true);
                        CoreMain.soundManager.playSound(player, SoundUtil.uiClick);
                        Bukkit.getScheduler().runTask(plugin, () -> CreateClaimDialog.open(player));
                    }), 0, 0);

                    var pageIndicator = UIUtil.makePageIndicator(gui, player, pages);

                    bottomBar.addItem(
                            pageIndicator, 4, 0
                    );

                    gui.addPane(Slot.fromXY(0, 0), pages);
                    gui.addPane(Slot.fromXY(0, 5), bottomBar);

                    Bukkit.getScheduler().runTask(plugin, bukkitTask -> {
                        gui.show(player);

                        UIUtil.updatePageUI(gui, player, pages, bottomBar, pageIndicator);
                    }
                    );
                }
        );
    }

    private static List<GuiItem> buildClaimItems(Player player, List<Claim> claims, boolean showOwner) {
        List<GuiItem> items = new ArrayList<>();

        for (Claim claim : claims) {
            Material material;
            switch (claim.getType()) {
                case PLAYER -> material = Material.PLAYER_HEAD;
                case TEAM -> material = Material.IRON_SWORD;
                case SERVER -> material = Material.COMMAND_BLOCK;
                case PLACEHOLDER -> material = Material.STRUCTURE_VOID;
                default -> material = Material.PAPER;
            }

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                AudienceUtil.sendMessage(player, Component.translatable("messages.claims.ui.errors.no-item-meta"));
                continue;
            }

            meta.addItemFlags(
                    ItemFlag.HIDE_ATTRIBUTES
            );

            meta.displayName(Component.text(claim.getName(), NamedTextColor.GREEN));

            List<Component> lore = new ArrayList<>();
            Component typeMessage;
            switch (claim.getType()) {
                case PLAYER -> typeMessage = Message.msg(player, "messages.claims.ui.select-menu.player-type");
                case TEAM -> typeMessage = Message.msg(player, "messages.claims.ui.select-menu.team-type");
                case SERVER -> typeMessage = Message.msg(player, "messages.claims.ui.select-menu.server-type");
                case PLACEHOLDER -> typeMessage = Message.msg(player, "messages.claims.ui.select-menu.placeholder-type");
                default -> typeMessage = Message.msg(player, "messages.claims.ui.select-menu.unknown-type");
            }

            lore.add(Component.text("Owner: "+ClaimManager.getOwnerName(claim)));

            lore.add(typeMessage);

            lore.add(
                    Message.msg(
                            player,
                        "messages.claims.ui.select-menu.id",
                            Map.of("id", String.valueOf(claim.getId()))));

            meta.lore(lore);
            item.setItemMeta(meta);

            items.add(new GuiItem(item, event -> {
                event.setCancelled(true);
                CoreMain.soundManager.playSound(player, SoundUtil.uiClick);
                ClaimEditMenu.open(player, claim);
            }));
        }

        return items;
    }
}