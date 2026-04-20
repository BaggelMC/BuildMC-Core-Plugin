package net.mathias2246.buildmc.ui;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.wrappers.BlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiConsumer;

import static net.mathias2246.buildmc.CoreMain.plugin;
import static net.mathias2246.buildmc.CoreMain.protocolManager;

public class SignInputScreen implements Listener {

    private static final Location signLocation = new Location(Bukkit.getWorlds().getFirst(), 0, 0, 0);

    private BlockPosition signPos;

    private PacketListener packetListener;

    private final BiConsumer<String, String> onInput;

    public SignInputScreen(@NotNull BiConsumer<String, String> onInput) {
        this.onInput = onInput;
    }

    private Player player;

    public void openSignInput(@NotNull Player player, @Nullable String footer, @NotNull String signId) {

        Location signLoc = signLocation.clone();
        signLoc.setX(player.getLocation().getX());
        signLoc.setZ(player.getLocation().getZ());
        signLoc.setY(player.getLocation().getY()-3);

        Sign sign = (Sign) Material.OAK_SIGN.createBlockData().createBlockState();

        SignSide d = sign.getSide(Side.FRONT);
        d.setLine(1, Objects.requireNonNullElse(footer, ""));

        player.sendBlockChange(signLoc, sign.getBlockData());
        player.sendBlockUpdate(signLoc, sign);

        sign.update(false, false);

        PacketContainer openSign = protocolManager.createPacket(PacketType.Play.Server.OPEN_SIGN_EDITOR);

        signPos = new BlockPosition(signLoc.getBlockX(), signLoc.getBlockY(), signLoc.getBlockZ());

        openSign.getBlockPositionModifier().write(0, signPos);
        openSign.getBooleans().write(0, true);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                this.player = player;
                Bukkit.getPluginManager().registerEvents(this, plugin);
                protocolManager.sendServerPacket(player, openSign);
                this.packetListener = new PacketAdapter(
                        plugin,
                        ListenerPriority.NORMAL,
                        PacketType.Play.Client.UPDATE_SIGN
                ) {
                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        PacketContainer packet = event.getPacket();

                        if (!event.getPlayer().equals(player) || !packet.getBooleans().read(0)) return;

                        BlockPosition pos = packet.getBlockPositionModifier().read(0);
                        if (!pos.equals(signPos)) return;

                        Bukkit.getScheduler().runTask(
                                plugin,
                                (task) -> {
                                    String[] sA = packet.getStringArrays().readSafely(0);
                                    if (!(sA == null || sA.length < 1)) {
                                        String s = sA[0];
                                        onInput.accept(signId, s == null ? "" : s);
                                    }

                                    player.sendBlockChange(signLoc, signLoc.getBlock().getBlockData());
                                }
                        );

                        protocolManager.removePacketListener(this);
                    }
                };
                protocolManager.addPacketListener(packetListener);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, 3L);

    }
}
