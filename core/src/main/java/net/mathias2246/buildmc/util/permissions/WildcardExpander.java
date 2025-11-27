package net.mathias2246.buildmc.util.permissions;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;

import java.util.*;

public final class WildcardExpander {

    private WildcardExpander() {}

    /**
     * Expands wildcard permissions such as:
     *   - "buildmc.*"
     *   - "*"
     * <p>
     * Uses ONLY Bukkit's registered permissions
     */
    public static Map<String, Boolean> expandWildcards(Map<String, Boolean> base) {

        Map<String, Boolean> result = new HashMap<>();

        // copy everything except wildcards
        for (var entry : base.entrySet()) {
            String node = entry.getKey();
            boolean value = entry.getValue();

            if (!isWildcard(node)) {
                result.put(node, value);
            }
        }

        // expand wildcards
        for (var entry : base.entrySet()) {
            String node = entry.getKey();
            boolean value = entry.getValue();

            if (node.equals("*")) {
                expandGlobalWildcard(result, value);
            } else if (node.endsWith(".*")) {
                expandPrefixWildcard(result, node, value);
            }
        }

        // add wildcard nodes as literal permissions
        result.putAll(base);

        return result;
    }

    private static boolean isWildcard(String node) {
        return node.equals("*") || node.endsWith(".*");
    }

    private static void expandGlobalWildcard(Map<String, Boolean> result, boolean value) {
        for (Permission perm : Bukkit.getPluginManager().getPermissions()) {
            result.put(perm.getName(), value);
        }
    }

    private static void expandPrefixWildcard(Map<String, Boolean> result, String wildcard, boolean value) {
        String prefix = wildcard.substring(0, wildcard.length() - 2); // strip .*

        for (Permission perm : Bukkit.getPluginManager().getPermissions()) {
            String name = perm.getName();
            if (name.startsWith(prefix + ".")) {
                result.put(name, value);
            }
        }
    }
}
