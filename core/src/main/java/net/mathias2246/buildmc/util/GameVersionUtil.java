package net.mathias2246.buildmc.util;

import org.bukkit.Bukkit;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameVersionUtil {

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

    public static boolean isAtLeast(String version, String requiredVersion) {
        int[] v = parseVersion(version);
        int[] r = parseVersion(requiredVersion);

        return v[0] >= r[0] && v[1] >= r[1] && v[2] >= r[2];
    }

    private static int[] parseVersion(String version) {
        String[] parts = version.split("\\.");
        int major = parts.length > 0 ? Integer.parseInt(parts[0]) : 0;
        int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
        return new int[] { major, minor, patch };
    }

}
