package net.mathias2246.buildmc.permissions;

import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.permissions.PermissionGroup;
import net.mathias2246.buildmc.util.permissions.WildcardExpander;
import net.mathias2246.buildmc.util.registry.DeferredRegistry;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class PermissionGroupLoader {

    private final DeferredRegistry<PermissionGroup> registry;
    private final File groupFolder;

    public PermissionGroupLoader(@NotNull DeferredRegistry<PermissionGroup> registry) {
        this.registry = registry;
        this.groupFolder = new File(CoreMain.plugin.getDataFolder(), "permissions/groups");
    }

    public void loadAll() {
        if (!groupFolder.exists() && !groupFolder.mkdirs()) {
            CoreMain.plugin.getLogger().severe("Could not create group folder: " + groupFolder.getAbsolutePath());
            return;
        }

        File[] files = groupFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            loadSingle(file);
        }

        ensureRequiredGroups();
    }

    private void loadSingle(@NotNull File file) {
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        String id = cfg.getString("name");
        if (id == null || id.isBlank()) {
            CoreMain.plugin.getLogger().warning("Group file " + file.getName() + " has no 'name:' field!");
            return;
        }

        int priority = cfg.getInt("priority", 0);

        Map<String, Boolean> permissions = new HashMap<>();
        var permSection = cfg.getConfigurationSection("permissions");

        if (permSection != null) {
            for (String key : permSection.getKeys(true)) {
                if (!permSection.isConfigurationSection(key)) {
                    permissions.put(key, permSection.getBoolean(key));
                }
            }
        }

        NamespacedKey key = PermissionGroupManager.idToNamespacedKey(id);
        if (key == null) {
            CoreMain.plugin.getLogger().warning("Invalid namespacedKey for group: " + id);
            return;
        }

        Map<String, Boolean> expanded = WildcardExpander.expandWildcards(permissions);

        registry.addEntry(new PermissionGroup(key, id, priority, expanded));
    }

    private void ensureRequiredGroups() {
        createGroupFileIfMissing("default", 0, Map.of("buildmc.player", true, "buildmc.allow-spawn-teleport", true));
        // createGroupFileIfMissing("operator", 99999, Map.of("*", true));
    }

    @SuppressWarnings("SameParameterValue")
    private void createGroupFileIfMissing(String id, int priority, Map<String, Boolean> perms) {
        File file = new File(groupFolder, id + ".yml");
        if (file.exists()) return;

        try {
            YamlConfiguration cfg = new YamlConfiguration();

            cfg.set("name", id);
            cfg.set("priority", priority);

            if (!perms.isEmpty()) {
                for (var entry : perms.entrySet()) {
                    cfg.set("permissions." + entry.getKey(), entry.getValue());
                }
            }

            cfg.save(file);

            CoreMain.plugin.getLogger().info(
                    "Generated missing required group file: " + file.getName()
            );
        } catch (Exception e) {
            CoreMain.plugin.getLogger().severe(
                    "Failed to generate required group file '" + id + "': " + e.getMessage()
            );
        }
    }
}