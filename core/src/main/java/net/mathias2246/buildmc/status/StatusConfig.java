package net.mathias2246.buildmc.status;

import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.status.StatusInstance;
import net.mathias2246.buildmc.util.config.YamlConfigurationManager;
import net.mathias2246.buildmc.util.registry.BaseRegistry;
import net.mathias2246.buildmc.util.registry.DefaultRegistries;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

/**The configuration that stores and manages all statuses available.*/
public class StatusConfig extends YamlConfigurationManager {

    /**
     * Creates a new StatusConfig instance from the existing './status.yml' or loads it from the plugins resources.
     * @param plugin The plugin owning this config*/
    public StatusConfig(Plugin plugin) {
        super(plugin, "status.yml");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setupConfiguration() {

        var reg = ((BaseRegistry<StatusInstance>) Objects.requireNonNull(CoreMain.registriesHolder.get(DefaultRegistries.STATUSES.toString())));

        for (var key : configuration.getKeys(false)) {
            //if (!configuration.isConfigurationSection(key)) return;

            var s = configuration.getConfigurationSection(key);
            if (s == null) return;
            var v = s.getValues(false);
            reg.set(Objects.requireNonNull(NamespacedKey.fromString("buildmc:" + key)), StatusInstance.deserialize(v, key));

        }
    }

    @Override
    protected void preSave() {

    }
}
