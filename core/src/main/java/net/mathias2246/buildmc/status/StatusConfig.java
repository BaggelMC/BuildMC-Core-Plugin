package net.mathias2246.buildmc.status;

import com.google.gson.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.status.StatusInstance;
import net.mathias2246.buildmc.util.config.YamlConfigurationManager;
import net.mathias2246.buildmc.util.registry.BaseRegistry;
import net.mathias2246.buildmc.util.registry.DefaultRegistries;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;

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
        Set<NamespacedKey> keys = reg.keySet();
        for (var key : keys) {
            reg.removeEntry(key);
        }

        for (var key : configuration.getKeys(false)) {
            //if (!configuration.isConfigurationSection(key)) return;

            var s = configuration.getConfigurationSection(key);
            if (s == null) return;
            var v = s.getValues(false);
            reg.addEntry(StatusInstance.deserialize(v, key.toLowerCase()));
        }
    }

    public static class StatusInstanceJsonDeserializer implements JsonDeserializer<StatusInstance> {

        @Override
        public StatusInstance deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject o = jsonElement.getAsJsonObject();
            String id = o.get("id").getAsString();
            JsonArray perms = o.get("permissions").getAsJsonArray();
            List<Permission> permissions = null;
            if (perms != null) {
                permissions = new ArrayList<>();
                for (var permissionStr : perms) {
                    permissions.add(new Permission(permissionStr.getAsString()));
                }
            }
            JsonArray t = o.getAsJsonArray("teams");
            List<Team> teams = null;
            if (t != null) {
                teams = new ArrayList<>();
                Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard();
                for (var teamStr : t) {
                    var ti = scoreboard.getTeam(teamStr.getAsString());
                    if (ti != null) teams.add(ti);
                }
            }
            Component displayName = MiniMessage.miniMessage().deserializeOr(o.get("display-name").getAsString(), null);
            if (displayName == null) CoreMain.plugin.getLogger().log(Level.WARNING, "Status with id '"+ id +"' has an empty display name!");
            return new StatusInstance(
                    id,
                    permissions == null ? null : Set.copyOf(permissions),
                    teams == null ? null : Set.copyOf(teams),
                    displayName
            );
        }
    }

    @Override
    protected void preSave() {

    }
}
