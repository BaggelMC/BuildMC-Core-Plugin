package net.mathias2246.buildmc.commands.debug;

import net.mathias2246.buildmc.CoreMain;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class InvalidateCaches {

    public static int invalidateCaches(@NotNull CommandSender sender) {
        CoreMain.claimTable.invalidateCache();
        CoreMain.permissionsTable.invalidateCache();

        sender.sendMessage("Invalidated all internal caches successfully.");

        return 1;
    }

}
