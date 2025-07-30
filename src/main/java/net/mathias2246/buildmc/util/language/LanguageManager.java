package net.mathias2246.buildmc.util.language;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LanguageManager {

    private static final Locale DEFAULT_LOCALE = Locale.forLanguageTag("en-US");

    private final JavaPlugin plugin;
    private final File langFolder;
    private final Logger logger;
    private final Map<Locale, Map<String, String>> translations = new HashMap<>();
    private boolean initialized = false;

    public LanguageManager(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.langFolder = new File(plugin.getDataFolder(), "lang");

        if (!langFolder.exists() && !langFolder.mkdirs()) {
            logger.severe("Could not create language folder: " + langFolder.getPath());
        }
    }

    public void init() {
        if (initialized) return;
        initialized = true;
        loadLanguages();
    }

    private void loadLanguages() {
        ensureDefaultLanguageFiles();

        File[] files = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                Yaml yaml = new Yaml();
                Map<String, Object> yamlData = yaml.load(reader);
                if (yamlData == null) continue;

                Locale locale = toLocale(file.getName());
                Map<String, String> flatMap = flatten("", yamlData);
                translations.put(locale, flatMap);

                logger.info("Loaded language file: " + file.getName());

            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to load language file: " + file.getName(), e);
            }
        }
    }

    private void ensureDefaultLanguageFiles() {
        List<String> defaultFiles = List.of("en-US.yml");

        for (String fileName : defaultFiles) {
            File targetFile = new File(langFolder, fileName);
            if (!targetFile.exists()) {
                try (InputStream in = plugin.getResource("lang/" + fileName)) {
                    if (in != null) {
                        Files.copy(in, targetFile.toPath());
                        logger.info("Copied default language file: " + fileName);
                    } else {
                        logger.warning("Default language file not found in JAR: " + fileName);
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Failed to copy default language file: " + fileName, e);
                }
            }
        }
    }

    private Locale toLocale(String filename) {
        String baseName = filename.replace(".yml", "").replace('-', '_');
        return Locale.forLanguageTag(baseName.replace('_', '-'));
    }

    private Map<String, String> flatten(String prefix, Map<?, ?> input) {
        Map<String, String> map = new HashMap<>();
        input.forEach((key, value) -> {
            String fullKey = prefix.isEmpty() ? key.toString() : prefix + "." + key;
            if (value instanceof Map<?, ?> nested) {
                map.putAll(flatten(fullKey, nested));
            } else {
                map.put(fullKey, value.toString());
            }
        });
        return map;
    }

    public BaseComponent[] translate(@NotNull Locale locale, @NotNull String key, @NotNull Map<String, String> placeholders) {
        String json = getRawMessage(locale, key);

        if (json == null) {
            json = getRawMessage(DEFAULT_LOCALE, key);
        }

        if (json == null) {
            return new BaseComponent[]{new TextComponent("Missing translation: " + key)};
        }

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            json = json.replace("%" + entry.getKey() + "%", entry.getValue());
        }

        try {
            return ComponentSerializer.parse(json);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to parse JSON message for key: " + key, e);
            return new BaseComponent[]{new TextComponent("Invalid JSON for key: " + key)};
        }
    }

    public String getRawMessage(Locale locale, String key) {
        Map<String, String> langMap = translations.get(locale);
        return langMap != null ? langMap.get(key) : null;
    }

    private Locale getLocaleForPlayer(Player player) {
        // You can replace this logic to support per-player locale
        return DEFAULT_LOCALE;
    }
}
