package net.mathias2246.buildmc.util;

import org.bukkit.Bukkit;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GameVersionUtil {

    public static void onVersion(String lowestRequiredVersion,  Consumer<String> onMatch) {

        Pattern pattern = Pattern.compile("MC:\\s*([0-9]+(?:\\.[0-9]+){1,2})");
        Matcher matcher = pattern.matcher(Bukkit.getServer().getVersion());

        if (matcher.find()) {
            String mcVersion = matcher.group(1);
            if (isAtLeast(lowestRequiredVersion, mcVersion)) {
                onMatch.accept(mcVersion);
            }
        }
    }

    /**
     * Returns true if {@code version} is at least {@code requiredVersion}.
     * <p>
     * Compatible with both version schemes:
     * <ul>
     *   <li>Old: {@code 1.Major.Patch} (e.g. {@code 1.21.4})</li>
     *   <li>New: {@code Year.Version.Hotfix} (e.g. {@code 26.1} or {@code 26.1.1})</li>
     * </ul>
     * Missing segments default to 0, so {@code 26.1} is treated as {@code 26.1.0}.
     */
    public static boolean isAtLeast(String version, String requiredVersion) {
        int[] v = parseVersion(version);
        int[] r = parseVersion(requiredVersion);

        // Compare lexicographically segment by segment,
        // only moving to the next segment if the current one is equal
        if (v[0] != r[0]) return v[0] > r[0];
        if (v[1] != r[1]) return v[1] > r[1];
        return v[2] >= r[2];
    }

    private static int[] parseVersion(String version) {
        try {
            String[] parts = version.split("\\.");
            int major = parts.length > 0 ? Integer.parseInt(parts[0]) : 0;
            int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
            return new int[]{major, minor, patch};
        } catch (NumberFormatException ignore) {
            return new int[]{999,999,999};
        }
    }

}
