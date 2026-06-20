package net.mathias2246.buildmc.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/** A utility class for Adventure-API {@link Component}s **/
public final class ComponentUtil {

    /** Tries to split {@link TextComponent}s on new-line characters.
     *
     * @param component The {@link Component} to split
     * @return A {@link List} of lines
     * **/
    public static @NotNull List<Component> splitComponentByNewline(@NotNull Component component) {
        // Fast-path: if the component has no newlines anywhere in its tree,
        // and it's a single logical unit, return it directly without rebuilding.
        if (!containsNewline(component)) {
            return List.of(component);
        }

        List<Component> lines = new ArrayList<>();
        List<Component> currentLine = new ArrayList<>();

        flattenComponent(component, currentLine, lines);

        if (!currentLine.isEmpty()) {
            lines.add(mergeComponents(currentLine));
        }

        return lines.isEmpty() ? List.of(Component.empty()) : lines;
    }

    private static boolean containsNewline(@NotNull Component component) {
        // Check this component's own content
        if (component instanceof TextComponent text
                && text.content().contains("\n")) {
            return true;
        }
        // Recurse into children
        for (Component child : component.children()) {
            if (containsNewline(child)) {
                return true;
            }
        }
        return false;
    }

    private static void flattenComponent(
            @NotNull Component component,
            @NotNull List<Component> currentLine,
            @NotNull List<Component> lines) {

        if (component instanceof TextComponent text) {
            String content = text.content();
            String[] parts = content.split("\n", -1);

            for (int i = 0; i < parts.length; i++) {
                if (i > 0) {
                    lines.add(mergeComponents(currentLine));
                    currentLine.clear();
                }
                if (!parts[i].isEmpty()) {
                    // Strip children here — we recurse into them separately below
                    currentLine.add(text.content(parts[i]).children(List.of()));
                }
            }
        } else {
            // Strip children but keep the component type and style
            currentLine.add(component.children(List.of()));
        }

        // Recurse into children
        for (Component child : component.children()) {
            flattenComponent(child, currentLine, lines);
        }
    }

    private static @NotNull Component mergeComponents(@NotNull List<Component> parts) {
        if (parts.isEmpty()) return Component.empty();
        if (parts.size() == 1) return parts.getFirst();

        // Use an unstyled root and attach all parts as children
        // This avoids base.children() wiping out base's own existing children
        return Component.text("").children(parts);
    }

}
