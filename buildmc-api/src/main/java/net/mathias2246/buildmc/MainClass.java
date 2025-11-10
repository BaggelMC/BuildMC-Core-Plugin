package net.mathias2246.buildmc;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Abstraction for sending messages to players in BuildMC.
 * <p>
 * This interface exists to provide a consistent way to send messages
 * to players using {@link Component}s, regardless of whether the
 * underlying plugin implementation is Paper or Spigot.
 * </p>
 * <p>
 * Implementations of this interface handle the differences between
 * server platforms internally, so external code can simply call
 * these methods without worrying about platform-specific APIs.
 * </p>
 */
public interface MainClass {

    /**
     * Sends a chat message to a player.
     *
     * @param player  the player to send the message to
     * @param message the message to send, as a {@link Component}
     */
    void sendMessage(CommandSender player, Component message);

    /**
     * Sends an action bar message to a player.
     *
     * @param player  the player to send the message to
     * @param message the message to send, as a {@link Component}
     */
    void sendPlayerActionBar(Player player, Component message);
}
