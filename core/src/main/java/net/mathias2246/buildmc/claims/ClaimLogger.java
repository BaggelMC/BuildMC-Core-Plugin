package net.mathias2246.buildmc.claims;

import net.mathias2246.buildmc.api.claims.Protection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public final class ClaimLogger {

    private static boolean enabled;
    private static boolean mirrorToConsole;
    private static int retentionDays;

    private static Path logDir;
    private static LocalTime rotationTime;

    private static LocalDate currentLogDate;
    private static Path currentLogFile;

    private static Plugin plugin;

    private static final ExecutorService LOG_EXECUTOR =
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "ClaimLogger-Async");
                t.setDaemon(true);
                return t;
            });

    private static final DateTimeFormatter FILE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter LOG_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ClaimLogger() {
        /* utility */
    }

    public static void init(Plugin pluginInstance) {
        plugin = pluginInstance;

        enabled = plugin.getConfig().getBoolean("claims.logs.enabled", true);
        retentionDays = plugin.getConfig().getInt("claims.logs.retention-days", 14);
        mirrorToConsole = plugin.getConfig().getBoolean("claims.logs.mirror-to-console", false);

        String rotationTimeStr =
                plugin.getConfig().getString("claims.logs.rotation-time", "00:00");

        rotationTime = LocalTime.parse(rotationTimeStr);
        logDir = plugin.getDataFolder().toPath().resolve("logs/claims");

        if (!enabled) return;

        // Initialize asynchronously
        LOG_EXECUTOR.execute(() -> {
            try {
                Files.createDirectories(logDir);
                rotateIfNeeded();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE,
                        "Failed to create claim log directory!", e);
                enabled = false;
            }
        });

        startRotationTask();
    }

    public static void shutdown() {
        LOG_EXECUTOR.shutdown();
        try {
            if (!LOG_EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                LOG_EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            LOG_EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static void startRotationTask() {
        long period = 20L * 60 * 10; // every 10 minutes

        plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                plugin,
                ClaimLogger::rotateIfNeeded,
                0L,
                period
        );
    }

    private static synchronized void rotateIfNeeded() {
        if (!enabled) return;

        LocalDate today = LocalDate.now();

        if (currentLogDate == null || !today.equals(currentLogDate)) {
            currentLogDate = today;
            currentLogFile = logDir.resolve(
                    "claims-" + FILE_DATE_FORMAT.format(currentLogDate) + ".log"
            );
            cleanupOldLogs();
        }
    }

    private static void cleanupOldLogs() {
        if (retentionDays < 0) return;

        try (var files = Files.list(logDir)) {
            LocalDate cutoff = LocalDate.now().minusDays(retentionDays);

            files.filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            String name = path.getFileName().toString();
                            if (!name.startsWith("claims-") || !name.endsWith(".log")) {
                                return;
                            }

                            String datePart = name.substring(
                                    "claims-".length(),
                                    name.length() - ".log".length()
                            );

                            LocalDate fileDate =
                                    LocalDate.parse(datePart, FILE_DATE_FORMAT);

                            if (fileDate.isBefore(cutoff)) {
                                Files.deleteIfExists(path);
                            }
                        } catch (Exception ignored) {
                        }
                    });
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING,
                    "Failed to clean up old claim logs", e);
        }
    }

    public static void log(String message) {
        if (!enabled) return;

        String timestamp = LocalDateTime.now().format(LOG_TIME_FORMAT);
        String line = "[" + timestamp + "] " + message + System.lineSeparator();

        LOG_EXECUTOR.execute(() -> {
            try {
                rotateIfNeeded();
                Files.writeString(
                        currentLogFile,
                        line,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND
                );
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE,
                        "Failed to write to claim log file", e);
            }
        });

        if (mirrorToConsole) {
            plugin.getLogger().info("[ClaimLogs] " + message);
        }
    }

    // ----------
    // Helpers
    // ----------

    private static String formatPlayer(Player player) {
        return player.getName() + " (" + player.getUniqueId() + ")";
    }

    public static void logClaimCreated(Player player, String claimName) {
        log(String.format(
                "[CLAIM CREATED] %s created claim '%s'",
                formatPlayer(player),
                claimName
        ));
    }

    public static void logClaimDeleted(Player player, String claimName) {
        log(String.format(
                "[CLAIM DELETED] %s deleted claim '%s'",
                formatPlayer(player),
                claimName
        ));
    }

    public static void logProtectionChanged(
            Player player,
            String claimName,
            String protectionName,
            String newValue
    ) {
        log(String.format(
                "[PROTECTION CHANGE] %s changed '%s' to '%s' in claim '%s'",
                formatPlayer(player),
                protectionName,
                newValue,
                claimName
        ));
    }

    public static void logProtectionChanged(
            Player player,
            String claimName,
            Protection protection,
            String newValue
    ) {
        logProtectionChanged(
                player,
                claimName,
                protection.getKey().toString(),
                newValue
        );
    }

    public static void logWhitelistAdded(
            Player player,
            String claimName,
            Player target
    ) {
        log(String.format(
                "[WHITELIST ADDED] %s added %s (%s) to claim '%s'",
                formatPlayer(player),
                target.getName(),
                target.getUniqueId(),
                claimName
        ));
    }

    public static void logWhitelistRemoved(
            Player player,
            String claimName,
            Player target
    ) {
        log(String.format(
                "[WHITELIST REMOVED] %s removed %s (%s) from claim '%s'",
                formatPlayer(player),
                target.getName(),
                target.getUniqueId(),
                claimName
        ));
    }

    public static void logWhitelistAdded(
            Player player,
            String claimName,
            String targetName,
            String targetUUID
    ) {
        log(String.format(
                "[WHITELIST ADDED] %s added %s (%s) to claim '%s'",
                formatPlayer(player),
                targetName,
                targetUUID,
                claimName
        ));
    }

    public static void logWhitelistRemoved(
            Player player,
            String claimName,
            String targetName,
            String targetUUID
    ) {
        log(String.format(
                "[WHITELIST REMOVED] %s removed %s (%s) from claim '%s'",
                formatPlayer(player),
                targetName,
                targetUUID,
                claimName
        ));
    }
}
