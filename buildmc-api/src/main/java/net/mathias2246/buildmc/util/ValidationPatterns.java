package net.mathias2246.buildmc.util;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public final class ValidationPatterns {
    /** Describes a valid YAML path. */
    public static final @NotNull Pattern YAML_PATH =
            Pattern.compile("^[A-Za-z0-9_-]+(?:\\.[A-Za-z0-9_-]+)*$");

    /** Describes a single word string, that can safely be used in command arguments. */
    public static final @NotNull Pattern VALID_STRING_IN_COMMAND =
            Pattern.compile("^[A-Za-z0-9_.-]+$");

    /** Describes a valid claim owner id. */
    public static final @NotNull Pattern VALID_CLAIM_OWNER =
            Pattern.compile("^(?:server|[A-Za-z0-9_.:-]+)$");

    /** Parses a valid minecraft version. */
    public static final @NotNull Pattern MINECRAFT_VERSION =
            Pattern.compile("MC:\\s*([0-9]+(?:\\.[0-9]+){1,2})");

}
