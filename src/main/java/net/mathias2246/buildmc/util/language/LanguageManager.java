package net.mathias2246.buildmc.util.language;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.Translator;
import net.mathias2246.buildmc.Main;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;
import static net.mathias2246.buildmc.Main.*;

import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.logging.Level;

public class LanguageManager {

    private static final Locale DEFAULT_LOCALE = Locale.forLanguageTag("en-US");
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final File LANG_FOLDER;
    private static final Key TRANSLATOR_KEY = Key.key("buildmc:lang");
    private static boolean initialized = false;

    static {
        LANG_FOLDER = new File(plugin.getDataFolder(), "lang");
        if (!LANG_FOLDER.exists() && !LANG_FOLDER.mkdirs()) {
            logger.severe("Could not create language folder: " + LANG_FOLDER.getPath());
        }
    }

    public static void init() {
        if (initialized) return;
        initialized = true;
        loadLanguages();
    }

    private static void loadLanguages() {
        ensureDefaultLanguageFiles();

        File[] files = LANG_FOLDER.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                Yaml yaml = new Yaml();
                Map<String, Object> yamlData = yaml.load(reader);
                if (yamlData == null) continue;

                Locale locale = toLocale(file.getName());
                MiniMessageTranslationStore store = MiniMessageTranslationStore.create(TRANSLATOR_KEY);

                flatten("", yamlData).forEach((key, value) -> {
                    if (value instanceof String str) {
                        store.register(key, locale, str);
                    }
                });

                GlobalTranslator.translator().addSource(store);
                logger.info("Loaded language file: " + file.getName());

            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to load language file: " + file.getName(), e);
            }
        }
    }

    private static void ensureDefaultLanguageFiles() {
        List<String> defaultFiles = List.of("en-US.yml");

        for (String fileName : defaultFiles) {
            File targetFile = new File(LANG_FOLDER, fileName);
            if (!targetFile.exists()) {
                try (var in = Main.class.getResourceAsStream("/lang/" + fileName)) {
                    if (in != null) {
                        java.nio.file.Files.copy(in, targetFile.toPath());
                    } else {
                        logger.warning("Default language file not found in JAR: " + fileName);
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Failed to copy default language file: " + fileName, e);
                }
            }
        }
    }

    private static Locale toLocale(String filename) {
        String baseName = filename.replace(".yml", "").replace('_', '-');
        return Locale.forLanguageTag(baseName);
    }

    private static Map<String, Object> flatten(String prefix, Map<?, ?> input) {
        Map<String, Object> map = new HashMap<>();
        input.forEach((key, value) -> {
            String fullKey = prefix.isEmpty() ? key.toString() : prefix + "." + key;
            if (value instanceof Map<?, ?> nested) {
                map.putAll(flatten(fullKey, nested));
            } else {
                map.put(fullKey, value);
            }
        });
        return map;
    }

    public static Component translate(@NotNull Locale playerLocale, @NotNull String key, @NotNull Map<String, String> replacements, Component... args) {
        Component base = Component.translatable(key, Arrays.asList(args));
        Component translated = GlobalTranslator.render(base, playerLocale);

        if (translated.equals(base)) {
            translated = GlobalTranslator.render(base, DEFAULT_LOCALE);
        }

        String serialized = MINI_MESSAGE.serialize(translated);
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            serialized = serialized.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        return MINI_MESSAGE.deserialize(serialized);
    }

    public static String translateStr(@NotNull Locale playerLocale, @NotNull String key, @NotNull Map<String, String> replacements, Component... args) {
        Component base = Component.translatable(key, Arrays.asList(args));
        Component translated = GlobalTranslator.render(base, playerLocale);

        if (translated.equals(base)) {
            translated = GlobalTranslator.render(base, DEFAULT_LOCALE);
        }

        String serialized = MINI_MESSAGE.serialize(translated);
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            serialized = serialized.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        return serialized;
    }


    public static Translator getTranslator() {
        return GlobalTranslator.translator();
    }
}
