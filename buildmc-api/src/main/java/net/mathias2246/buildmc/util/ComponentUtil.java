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
        List<Component> lines = new ArrayList<>();
        List<Component> currentLine = new ArrayList<>();

        // Flatten the component tree into a single list of leaves
        // so we can scan for newlines while preserving all styling
        flattenComponent(component, currentLine, lines);

        // Flush the last line if it has content
        if (!currentLine.isEmpty()) {
            lines.add(mergeComponents(currentLine));
        }

        return lines.isEmpty() ? List.of(Component.empty()) : lines;
    }

    private static void flattenComponent(
            @NotNull Component component,
            @NotNull List<Component> currentLine,
            @NotNull List<Component> lines) {

        // Check if this component's content contains a newline
        if (component instanceof TextComponent text) {
            String content = text.content();
            String[] parts = content.split("\n", -1);

            for (int i = 0; i < parts.length; i++) {
                if (i > 0) {
                    // We hit a newline — flush current line and start a new one
                    lines.add(mergeComponents(currentLine));
                    currentLine.clear();
                }

                if (!parts[i].isEmpty()) {
                    // Preserve the original component's style on each split part
                    currentLine.add(text.content(parts[i]));
                }
            }
        } else {
            // Non-text components (translatable, keybind, etc.)
            // can't contain newlines themselves, add as-is
            currentLine.add(component.children(List.of()));
        }

        // Recurse into children, inheriting parent style
        for (Component child : component.children()) {
            flattenComponent(child, currentLine, lines);
        }
    }

    private static @NotNull Component mergeComponents(@NotNull List<Component> parts) {
        if (parts.isEmpty()) return Component.empty();
        if (parts.size() == 1) return parts.getFirst();

        // Use the first part as the base and attach the rest as children
        Component base = parts.getFirst();
        return base.children(parts.subList(1, parts.size()));
    }

}
