package net.mathias2246.buildmc.api.messageBus;

import org.jspecify.annotations.NonNull;

public record SemVer(int major, int minor, int patch) {

    // Parse from "1.2.0" or "1.2" (patch defaults to 0)
    public static SemVer parse(String version) {
        String[] parts = version.split("\\.");
        if (parts.length < 2 || parts.length > 3) {
            throw new IllegalArgumentException("Invalid version string: '" + version + "'");
        }
        try {
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            int patch = parts.length == 3 ? Integer.parseInt(parts[2]) : 0;
            return new SemVer(major, minor, patch);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid version string: '" + version + "'", e);
        }
    }

    public static SemVer of(int major, int minor, int patch) {
        return new SemVer(major, minor, patch);
    }

    // Is `other` compatible with what we published?
    // Same major, and we published >= what they expect
    public boolean isCompatibleWith(SemVer other) {
        return this.major == other.major && this.minor >= other.minor;
    }

    @Override
    public @NonNull String toString() {
        return major + "." + minor + "." + patch;
    }
}