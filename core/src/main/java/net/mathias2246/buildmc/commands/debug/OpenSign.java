package net.mathias2246.buildmc.commands.debug;

import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.ui.SignInputScreen;
import net.mathias2246.buildmc.util.AudienceUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class OpenSign {

    public static void openFakeSign(@NotNull Player player) {
        SignInputScreen in = new SignInputScreen(
                (signId, output) -> AudienceUtil.sendMessage(player, Component.text("Input for sign '"+signId+"' was:\n"+output))
        );
        in.openSignInput(player, "Input:", "test_sign");
    }

}
