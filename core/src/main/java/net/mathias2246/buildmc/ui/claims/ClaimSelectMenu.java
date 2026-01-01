package net.mathias2246.buildmc.ui.claims;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.claims.ClaimType;
import net.mathias2246.buildmc.claims.ClaimManager;
import net.mathias2246.buildmc.inventoryframework.adventuresupport.ComponentHolder;
import net.mathias2246.buildmc.inventoryframework.gui.GuiItem;
import net.mathias2246.buildmc.inventoryframework.gui.type.ChestGui;
import net.mathias2246.buildmc.inventoryframework.pane.PaginatedPane;
import net.mathias2246.buildmc.inventoryframework.pane.StaticPane;
import net.mathias2246.buildmc.ui.UIUtil;
import net.mathias2246.buildmc.util.Message;
import net.mathias2246.buildmc.util.SoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static net.mathias2246.buildmc.CoreMain.plugin;
import static net.mathias2246.buildmc.ui.UIUtil.LEGACY_COMPONENT_SERIALIZER;

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

                    PaginatedPane pages = new PaginatedPane(0, 0, 9, 5);

                    pages.populateWithGuiItems(claimButtons);

                    StaticPane bottomBar = UIUtil.BOTTOM_BAR.copy();

                    bottomBar.addItem(UIUtil.EXIT_BUTTON, 0, 0);

                    var pageIndicator = UIUtil.makePageIndicator(gui, player, pages);

                    bottomBar.addItem(
                            pageIndicator, 4, 0
                    );

                    gui.addPane(pages);
                    gui.addPane(bottomBar);

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
                plugin.sendMessage(player, Component.translatable("messages.claims.ui.errors.no-item-meta"));
                continue;
            }

            meta.addItemFlags(
                    ItemFlag.HIDE_ATTRIBUTES,
                    ItemFlag.HIDE_ADDITIONAL_TOOLTIP
            );

            meta.setDisplayName(LEGACY_COMPONENT_SERIALIZER.serialize(Component.text(claim.getName(), NamedTextColor.GREEN)));

            List<String> lore = new ArrayList<>();
            String typeMessage;
            switch (claim.getType()) {
                case PLAYER -> typeMessage = LEGACY_COMPONENT_SERIALIZER.serialize(Message.msg(player, "messages.claims.ui.select-menu.player-type"));
                case TEAM -> typeMessage = LEGACY_COMPONENT_SERIALIZER.serialize(Message.msg(player, "messages.claims.ui.select-menu.team-type"));
                case SERVER -> typeMessage = LEGACY_COMPONENT_SERIALIZER.serialize(Message.msg(player, "messages.claims.ui.select-menu.server-type"));
                case PLACEHOLDER -> typeMessage = LEGACY_COMPONENT_SERIALIZER.serialize(Message.msg(player, "messages.claims.ui.select-menu.placeholder-type"));
                default -> typeMessage = LEGACY_COMPONENT_SERIALIZER.serialize(Message.msg(player, "messages.claims.ui.select-menu.unknown-type"));
            }

            lore.add("Owner: "+getOwnerDisplayName(claim));

            lore.add(typeMessage);

            lore.add(LEGACY_COMPONENT_SERIALIZER.serialize(Message.msg(player,
                    "messages.claims.ui.select-menu.id",
                    Map.of("id", String.valueOf(claim.getId())))));

            meta.setLore(lore);
            item.setItemMeta(meta);

            items.add(new GuiItem(item, event -> {
                event.setCancelled(true);
                CoreMain.soundManager.playSound(player, SoundUtil.uiClick);
                ClaimEditMenu.open(player, claim);
            }));
        }

        return items;
    }

    private static String getOwnerDisplayName(Claim claim) {
        ClaimType claimType = claim.getType();

        if (claimType == ClaimType.TEAM) return claim.getOwnerId();
        else if (claimType == ClaimType.PLAYER) {
            UUID ownerId = UUID.fromString(claim.getOwnerId());
            OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerId);
            String ownerName = owner.getName();

            if (ownerName == null) {
                ownerName = "Unknown ("+ownerId+")";
            }

            return ownerName;
        } else if (claimType == ClaimType.SERVER || claimType == ClaimType.PLACEHOLDER) {
            return "Server";
        }
        return "Unknown";
    }
}