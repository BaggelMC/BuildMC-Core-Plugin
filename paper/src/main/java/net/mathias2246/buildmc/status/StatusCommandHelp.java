package net.mathias2246.buildmc.status;

import org.bukkit.command.CommandSender;
import org.bukkit.help.HelpTopic;
import org.jetbrains.annotations.NotNull;

public class StatusCommandHelp extends HelpTopic {

    public StatusCommandHelp() {
        this.name = "/status";
        this.fullText = "Use this command to set your status. Your status is visible in the tablist and beside your name.";
        this.shortText = "Set or remove your status.";
    }

    @Override
    public boolean canSee(@NotNull CommandSender commandSender) {
        return true;
    }
}
