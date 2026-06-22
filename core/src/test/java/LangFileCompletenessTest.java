import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Validates that every language file under core/src/main/resources/lang/
 * contains all keys present in the reference file (en-US.yml).
 *
 * <p>Each non-reference .yml file gets its own {@link DynamicTest}, so failures
 * are reported per file rather than stopping at the first missing key.
 */
@DisplayName("Language File Completeness")
class LangFileCompletenessTest {

    private static final String REFERENCE_FILE = "en-US.yml";

    /** Resolved at test startup; fails fast if the directory cannot be found. */
    private static Path langDir;
    private static Set<String> referenceKeys;

    // -------------------------------------------------------------------------
    // Setup
    // -------------------------------------------------------------------------

    @BeforeAll
    static void locateLangDirectory() throws URISyntaxException, IOException {
        /*
         * Mirrors the Python script's path resolution:
         *   <repo-root>/core/src/main/resources/lang
         *
         * We walk up from the test-classes directory until we find the repo root
         * (identified by the presence of "core/src/main/resources/lang").
         */
        Path start = Path.of(
                LangFileCompletenessTest.class
                        .getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
                        .toURI()
        );

        langDir = resolveRepoLangDir(start);

        if (langDir == null || !Files.isDirectory(langDir)) {
            fail("Language folder not found. Searched upward from: " + start);
        }

        Path referencePath = langDir.resolve(REFERENCE_FILE);
        if (!Files.exists(referencePath)) {
            fail("Reference file not found in lang folder: " + referencePath);
        }

        referenceKeys = flattenKeys(loadYaml(referencePath));
        System.out.printf("Loaded reference file: %s (%d keys)%n",
                REFERENCE_FILE, referenceKeys.size());
    }

    // -------------------------------------------------------------------------
    // Test factory – one dynamic test per non-reference language file
    // -------------------------------------------------------------------------

    @TestFactory
    @DisplayName("All keys present in")
    Stream<DynamicTest> allLanguageFilesAreComplete() throws IOException {
        List<Path> langFiles;
        try (var stream = Files.list(langDir)) {
            langFiles = stream
                    .filter(p -> p.getFileName().toString().endsWith(".yml"))
                    .filter(p -> !p.getFileName().toString().equals(REFERENCE_FILE))
                    .sorted()
                    .toList();
        }

        if (langFiles.isEmpty()) {
            // Nothing to test besides the reference – that is itself suspicious,
            // but not a failure of completeness.
            return Stream.empty();
        }

        return langFiles.stream().map(path -> {
            String fileName = path.getFileName().toString();

            return DynamicTest.dynamicTest(fileName, () -> {
                Set<String> keys = flattenKeys(loadYaml(path));
                Set<String> missing = referenceKeys.stream()
                        .filter(k -> !keys.contains(k))
                        .collect(Collectors.toSet());

                if (!missing.isEmpty()) {
                    List<String> sorted = new ArrayList<>(missing);
                    Collections.sort(sorted);
                    String missingList = sorted.stream()
                            .map(k -> "  - " + k)
                            .collect(Collectors.joining("\n"));

                    fail(String.format(
                            "%s is missing %d key(s):%n%s",
                            fileName, missing.size(), missingList
                    ));
                }

                // Explicit pass message visible in IDE test output
                assertTrue(true, fileName + " is complete!");
            });
        });
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Loads a YAML file and returns its contents as a nested map.
     * Returns an empty map for null/empty files (mirrors {@code yaml.safe_load(...) or {}}).
     */
    private static Map<String, Object> loadYaml(Path path) throws IOException {
        Yaml yaml = new Yaml();
        try (FileInputStream fis = new FileInputStream(path.toFile())) {
            Map<String, Object> result = yaml.load(fis);
            return result != null ? result : Collections.emptyMap();
        }
    }

    /**
     * Recursively flattens a nested map into dot-separated keys,
     * e.g. {@code {a: {b: "v"}}} → {@code ["a.b"]}.
     */
    private static Set<String> flattenKeys(Map<String, Object> map) {
        return flattenKeysRecursive(map, "").collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    private static Stream<String> flattenKeysRecursive(Map<String, Object> map, String prefix) {
        return map.entrySet().stream().flatMap(entry -> {
            String fullKey = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            if (entry.getValue() instanceof Map<?, ?> nested) {
                return flattenKeysRecursive((Map<String, Object>) nested, fullKey);
            }
            return Stream.of(fullKey);
        });
    }

    /**
     * Walks upward from {@code start} until it finds a directory that contains
     * {@code core/src/main/resources/lang}, matching the Python script's
     * {@code os.path.join(script_dir, "..", "..", "core", "src", "main", "resources", "lang")}.
     */
    private static Path resolveRepoLangDir(Path start) {
        Path candidate = start;
        while (candidate != null) {
            Path langPath = candidate.resolve(
                    Path.of("core", "src", "main", "resources", "lang"));
            if (Files.isDirectory(langPath)) {
                return langPath;
            }
            candidate = candidate.getParent();
        }
        return null;
    }
}