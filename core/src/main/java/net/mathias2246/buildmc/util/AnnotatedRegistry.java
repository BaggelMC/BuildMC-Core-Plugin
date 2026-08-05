package net.mathias2246.buildmc.util;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ClassGraph;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public final class AnnotatedRegistry {

    private AnnotatedRegistry() {}

    /**
     *
     * @throws IllegalStateException ...
     * @throws RuntimeException ...
     * **/
    public static <T> Set<T> load(
            FileConfiguration config,
            String basePackage,
            Class<T> type,
            Class<? extends Annotation> annotationType,
            Function<AnnotationInfo, String> pathExtractor,
            Function<AnnotationInfo, Boolean> defaultEnabledExtractor
    ) {
        var set = new HashSet<T>();

        try (var scan = new ClassGraph()
                .enableAnnotationInfo()
                .acceptPackages(basePackage)
                .scan()) {

            for (var classInfo : scan.getClassesWithAnnotation(annotationType)) {
                boolean valid = classInfo.extendsSuperclass(type)
                        || (type.isInterface() && classInfo.implementsInterface(type));

                if (!valid) {
                    throw new IllegalStateException(classInfo.getName() + " is annotated but does not implement/extend " + type.getName());
                }

                var annotationInfo = classInfo.getAnnotationInfo(annotationType);
                var path = pathExtractor.apply(annotationInfo);
                var defaultEnabled = defaultEnabledExtractor.apply(annotationInfo);

                var section = config.getConfigurationSection(path);
                boolean enabled = section == null ? defaultEnabled : section.getBoolean("enabled", defaultEnabled);
                if (!enabled) continue; // never constructed if disabled

                var loaded = classInfo.loadClass(type);
                try {
                    var ctor = loaded.getConstructor(ConfigurationSection.class);
                    set.add(ctor.newInstance(section));
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Failed to construct " + loaded.getName(), e);
                }
            }
        }

        return set;
    }

    public static <T> Set<T> loadSafe(
            FileConfiguration config,
            String basePackage,
            Class<T> type,
            Class<? extends Annotation> annotationType,
            Function<AnnotationInfo, String> pathExtractor,
            Function<AnnotationInfo, Boolean> defaultEnabledExtractor
    ) {
        var set = new HashSet<T>();

        try (var scan = new ClassGraph()
                .enableAnnotationInfo()
                .acceptPackages(basePackage)
                .scan()) {

            for (var classInfo : scan.getClassesWithAnnotation(annotationType)) {
                boolean valid = classInfo.extendsSuperclass(type)
                        || (type.isInterface() && classInfo.implementsInterface(type));

                if (!valid) {
                    continue;
                }

                var annotationInfo = classInfo.getAnnotationInfo(annotationType);
                var path = pathExtractor.apply(annotationInfo);
                var defaultEnabled = defaultEnabledExtractor.apply(annotationInfo);

                var section = config.getConfigurationSection(path);
                boolean enabled = section == null ? defaultEnabled : section.getBoolean("enabled", defaultEnabled);
                if (!enabled) continue; // never constructed if disabled

                var loaded = classInfo.loadClass(type);
                try {
                    var ctor = loaded.getConstructor(ConfigurationSection.class);
                    set.add(ctor.newInstance(section));
                } catch (ReflectiveOperationException ignore) {
                    continue;
                }
            }
        }

        return set;
    }
}
