package net.mathias2246.buildmc.commands;

import dev.jorel.commandapi.CommandAPI;
import net.mathias2246.buildmc.CoreMain;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class CommandRegister implements Listener {

    public static void setupCommandAPI() {
        CommandAPIConfig cmdConfig = new CommandAPIConfig(CoreMain.plugin);
        cmdConfig.setup();

        CommandAPI.onLoad(
                cmdConfig
        );
        CommandAPI.onEnable();
    }

   public static void register(@NotNull CustomCommand command) {
       command.getCommand().register("buildmc");
   }

}
