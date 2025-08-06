package net.mathias2246.buildmc;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public interface MainClass {
    void sendPlayerMessage(Player player, Component message);

    void sendPlayerActionBar(Player player, Component message);
}
