package net.mathias2246.buildmc.commands.debug;

import net.mathias2246.buildmc.ui.SignInputScreen;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class OpenSign {

    public static int openFakeSign(@NotNull Player player) {
        SignInputScreen in = new SignInputScreen(
                (signId, output) -> {
                    player.sendMessage("Input for sign '"+signId+"' was:\n"+output);
                }
        );
        in.openSignInput(player, "Input:", "test_sign");
        return 1;
    }

}
