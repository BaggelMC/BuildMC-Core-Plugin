package net.mathias2246.buildmc.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ComponentUtilTest {

    @Test
    void emptyComponent_returnsSingleEmptyLine() {
        List<Component> result = ComponentUtil.splitComponentByNewline(Component.empty());
        assertEquals(1, result.size());
    }

    @Test
    void plainText_noNewline_returnsSingleComponent() {
        Component input = Component.text("Hello, world!");
        List<Component> result = ComponentUtil.splitComponentByNewline(input);

        assertEquals(1, result.size());
        assertTextContent(result.getFirst(), "Hello, world!");
    }

    @Test
    void plainText_singleNewline_returnsTwoLines() {
        Component input = Component.text("line1\nline2");
        List<Component> result = ComponentUtil.splitComponentByNewline(input);

        assertEquals(2, result.size());
        assertTextContent(result.get(0), "line1");
        assertTextContent(result.get(1), "line2");
    }

    @Test
    void plainText_multipleNewlines_returnsCorrectLineCount() {
        Component input = Component.text("a\nb\nc");
        List<Component> result = ComponentUtil.splitComponentByNewline(input);

        assertEquals(3, result.size());
        assertTextContent(result.get(0), "a");
        assertTextContent(result.get(1), "b");
        assertTextContent(result.get(2), "c");
    }

    @Test
    void leadingNewline_firstLineIsEmpty_skippedInOutput() {
        // The current implementation skips empty parts, so a leading newline
        // produces a flush of an empty currentLine -> empty component added as line
        Component input = Component.text("\nline2");
        List<Component> result = ComponentUtil.splitComponentByNewline(input);

        // First flush produces an empty list -> mergeComponents([]) -> Component.empty()
        assertEquals(2, result.size());
        assertTextContent(result.get(1), "line2");
    }

    @Test
    void trailingNewline_lastLineIsEmpty_skippedInOutput() {
        Component input = Component.text("line1\n");
        List<Component> result = ComponentUtil.splitComponentByNewline(input);

        // "line1" is on line 0; the trailing newline flushes it, currentLine is then
        // empty at the end so nothing extra is added
        assertEquals(1, result.size());
        assertTextContent(result.getFirst(), "line1");
    }

    @Test
    void onlyNewline_returnsEmptyComponent() {
        Component input = Component.text("\n");
        List<Component> result = ComponentUtil.splitComponentByNewline(input);

        // Splits into ["", ""] — both parts are empty, so currentLine is never
        // populated. The fallback List.of(Component.empty()) kicks in
        assertEquals(1, result.size());
    }

    @Test
    void consecutiveNewlines_producesBlankLinesBetween() {
        Component input = Component.text("a\n\nb");
        List<Component> result = ComponentUtil.splitComponentByNewline(input);

        // parts = ["a", "", "b"]
        // i=0 "a"  -> currentLine = [text("a")]
        // i=1 ""   -> flush: lines = [text("a")], currentLine = []
        //            empty part -> nothing added to currentLine
        //            But the flush itself calls mergeComponents([]) = Component.empty(),
        //            so the blank line IS recorded.
        // i=2 "b"  -> currentLine = [text("b")]
        // End      -> flush: lines = [text("a"), Component.empty(), text("b")]
        assertEquals(3, result.size());
        assertTextContent(result.get(0), "a");
        // Middle line is a blank/empty component — just verify it exists
        assertNotNull(result.get(1));
        assertTextContent(result.get(2), "b");
    }

    @Test
    void styledText_styleIsPreservedAfterSplit() {
        Component input = Component.text("bold1\nbold2", NamedTextColor.RED, TextDecoration.BOLD);
        List<Component> result = ComponentUtil.splitComponentByNewline(input);

        assertEquals(2, result.size());

        TextComponent line0 = (TextComponent) result.getFirst();
        assertEquals("bold1", line0.content());
        assertEquals(NamedTextColor.RED, line0.color());
        assertTrue(line0.hasDecoration(TextDecoration.BOLD));

        TextComponent line1 = (TextComponent) result.get(1);
        assertEquals("bold2", line1.content());
        assertEquals(NamedTextColor.RED, line1.color());
        assertTrue(line1.hasDecoration(TextDecoration.BOLD));
    }

    @Test
    void unstyledText_styleIsAbsent() {
        Component input = Component.text("plain");
        List<Component> result = ComponentUtil.splitComponentByNewline(input);

        TextComponent line = (TextComponent) result.getFirst();
        assertNull(line.color());
    }

    @Test
    void newlineInChild_splitAcrossParentAndChild() {
        // Parent has no newline; child introduces the break
        Component input = Component.text("parent ")
                .append(Component.text("child1\nchild2"));

        List<Component> result = ComponentUtil.splitComponentByNewline(input);

        assertEquals(2, result.size());
        // Line 0 should contain "parent " merged with "child1"
        // Line 1 should be "child2"
        assertTextContent(result.get(1), "child2");
    }

    @Test
    void newlineInParent_childOnSecondLine() {
        Component input = Component.text("line1\n")
                .append(Component.text("line2"));

        List<Component> result = ComponentUtil.splitComponentByNewline(input);

        assertEquals(2, result.size());
        assertTextContent(result.get(0), "line1");
        assertTextContent(result.get(1), "line2");
    }

    @Test
    void childWithDifferentColor_preservesChildStyle() {
        Component input = Component.text("prefix ")
                .append(Component.text("colored", NamedTextColor.GREEN));

        List<Component> result = ComponentUtil.splitComponentByNewline(input);

        assertEquals(1, result.size());
        // The result is a merged component; verify the colored child is reachable
        Component merged = result.getFirst();
        // Either the base or one of its children should carry the green text
        assertTrue(containsColoredText(merged, "colored", NamedTextColor.GREEN),
                "Expected a descendant with green 'colored' text");
    }

    @Test
    void multipleChildren_noNewlines_allOnOneLine() {
        Component input = Component.text("a")
                .append(Component.text("b"))
                .append(Component.text("c"));

        List<Component> result = ComponentUtil.splitComponentByNewline(input);

        assertEquals(1, result.size());
    }

    @Test
    void multipleChildren_eachWithNewline_correctLineCount() {
        Component input = Component.text("1\n2")
                .append(Component.text("\n3\n4"));

        List<Component> result = ComponentUtil.splitComponentByNewline(input);

        // "1" -> line, "2" + "" (from child split) -> flush -> "3" -> line, "4" -> final flush
        assertEquals(4, result.size());
    }

    @Test
    void deeplyNestedChildren_flattenedCorrectly() {
        Component innermost = Component.text("deep");
        Component middle   = Component.text("mid\n").append(innermost);
        Component outer    = Component.text("top\n").append(middle);

        List<Component> result = ComponentUtil.splitComponentByNewline(outer);

        assertEquals(3, result.size());
        assertTextContent(result.getFirst(), "top");
    }

    @Test
    void singlePartLine_isReturnedDirectly() {
        // When a line contains only one component part, mergeComponents returns it
        // as-is (no unnecessary wrapping)
        Component input = Component.text("only");
        List<Component> result = ComponentUtil.splitComponentByNewline(input);

        assertSame(input, result.getFirst(),
                "Single-element line should return the original component unchanged");
    }

    @Test
    void multiPartLine_firstPartIsBaseWithRestAsChildren() {
        // Parent "a" + child "b", no newlines -> merged into one line where "b"
        // becomes a child of "a"
        Component a = Component.text("a");
        Component b = Component.text("b");
        Component input = a.append(b);

        List<Component> result = ComponentUtil.splitComponentByNewline(input);

        assertEquals(1, result.size());
        Component merged = result.getFirst();
        // The merged component should expose both texts when traversed
        assertTrue(collectText(merged).contains("a"));
        assertTrue(collectText(merged).contains("b"));
    }

    // Helpers

    /** Asserts that {@code component} is a {@link TextComponent} with the given content. */
    private static void assertTextContent(Component component, String expected) {
        assertInstanceOf(TextComponent.class, component,
                "Expected TextComponent but got " + component.getClass().getSimpleName());
        assertEquals(expected, ((TextComponent) component).content());
    }

    /**
     * Collects all text content from a component tree (breadth-first)
     */
    private static String collectText(Component root) {
        StringBuilder sb = new StringBuilder();
        if (root instanceof TextComponent tc) sb.append(tc.content());
        for (Component child : root.children()) sb.append(collectText(child));
        return sb.toString();
    }

    /**
     * Returns true if any node in the component tree has the given text content and color
     */
    private static boolean containsColoredText(Component root, String text, NamedTextColor color) {
        if (root instanceof TextComponent tc
                && tc.content().equals(text)
                && color.equals(tc.color())) {
            return true;
        }
        for (Component child : root.children()) {
            if (containsColoredText(child, text, color)) return true;
        }
        return false;
    }
}
