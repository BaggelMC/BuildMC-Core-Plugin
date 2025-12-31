package net.mathias2246.buildmc.commands;

import dev.jorel.commandapi.CommandAPIBukkitConfig;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandAPIConfig extends CommandAPIBukkitConfig {
        public CommandAPIConfig(JavaPlugin plugin) {
            super(plugin);

        }

        public void setup() {
            this.setNamespace("buildmc");
        }

        @Override
        public CommandAPIConfig instance() {
            return this;
        }
    }