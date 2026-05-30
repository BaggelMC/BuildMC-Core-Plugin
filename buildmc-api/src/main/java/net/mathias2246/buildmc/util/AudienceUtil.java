package net.mathias2246.buildmc.util;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public final class AudienceUtil {

    public static void sendMessage(@NotNull Audience audience, Component message) {
        audience.sendMessage(message);
    }

    public static void sendActionBar(@NotNull Audience audience, Component message) {
        audience.sendActionBar(message);
    }

}
