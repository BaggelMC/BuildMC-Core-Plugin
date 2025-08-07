package net.mathias2246.buildmc.status;


import net.mathias2246.buildmc.util.config.YamlConfigurationManager;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

/**The configuration that stores and manages all statuses available.*/
public class StatusConfig extends YamlConfigurationManager {
    /**A map containing all Statuses that are loaded.*/
    public static final Map<String, StatusInstance> loadedStatuses = new HashMap<>();

    /**
     * Creates a new StatusConfig instance from the existing './status.yml' or loads it from the plugins resources.
     * @param plugin The plugin owning this config*/
    public StatusConfig(Plugin plugin) {
        super(plugin, "status.yml");
    }

    @Override
    public void setupConfiguration() {
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
