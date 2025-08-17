package net.mathias2246.buildmc.status;


import net.mathias2246.buildmc.util.ConfigurationManager;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class StatusConfig extends ConfigurationManager {

    public static final Map<String, StatusInstance> loadedStatuses = new HashMap<>();

    public StatusConfig(Plugin plugin) {
        super(plugin, "status.yml");
    }

    @Override
    public void setupConfiguration() {
        loadedStatuses.clear();
        for (var key : configuration.getKeys(false)) {
            //if (!configuration.isConfigurationSection(key)) return;

            var s = configuration.getConfigurationSection(key);
            if (s == null) return;
            var v = s.getValues(false);
            loadedStatuses.put(key, StatusInstance.deserialize(v));
        }
    }

    @Override
    protected void preSave() {

    }
}
