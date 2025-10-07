package net.mathias2246.buildmc.claims;

import net.mathias2246.buildmc.api.claims.Protection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;

public class ClaimLogger {

    private static boolean enabled;
    private static Path logDir;
    private static int retentionDays;
    private static LocalTime rotationTime;
    private static boolean mirrorToConsole;
    private static LocalDate currentLogDate;
    private static Path currentLogFile;
    private static Plugin plugin;

    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter LOG_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void init(Plugin pluginInstance) {
        plugin = pluginInstance;

        enabled = plugin.getConfig().getBoolean("claims.logs.enabled", true);
        retentionDays = plugin.getConfig().getInt("claims.logs.retention-days", 14);
        String rotationTimeStr = plugin.getConfig().getString("claims.logs.rotation-time", "00:00");
        mirrorToConsole = plugin.getConfig().getBoolean("claims.logs.mirror-to-console", false);

        rotationTime = LocalTime.parse(rotationTimeStr);
        logDir = plugin.getDataFolder().toPath().resolve("logs/claims");

        if (!enabled) return;

        try {
            Files.createDirectories(logDir);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create claim log directory!", e);
            enabled = false;
            return;
        }

        rotateIfNeeded();
        startRotationTask();
    }

    private static void startRotationTask() {
        long ticksUntilNextCheck = 20L * 60 * 10; // every 10 minutes
        plugin.getServer().getScheduler().runTaskTimer(plugin, ClaimLogger::rotateIfNeeded, 0, ticksUntilNextCheck);
    }

    private static long computeTicksUntilNextRotation() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next = now.toLocalDate().atTime(rotationTime);
        if (!next.isAfter(now)) next = next.plusDays(1);
        Duration duration = Duration.between(now, next);
        return duration.getSeconds() * 20;
    }

    private static synchronized void rotateIfNeeded() {
        LocalDateTime now = LocalDateTime.now();
        if (!now.toLocalDate().equals(currentLogDate) ||
                now.toLocalTime().isAfter(rotationTime) && currentLogDate.equals(LocalDate.now().minusDays(1))) {

            currentLogDate = now.toLocalDate();
            currentLogFile = logDir.resolve("claims-" + FILE_DATE_FORMAT.format(currentLogDate) + ".log");
            cleanupOldLogs();
        }
    }

    private static void cleanupOldLogs() {
        if (retentionDays < 0) return;

        try (var files = Files.list(logDir)) {
            files.filter(Files::isRegularFile)
                    .filter(path -> {
                        try {
                            String name = path.getFileName().toString();
                            String datePart = name.replace("claims-", "").replace(".log", "");
                            LocalDate fileDate = LocalDate.parse(datePart, FILE_DATE_FORMAT);
                            return fileDate.isBefore(LocalDate.now().minusDays(retentionDays));
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException ignored) {}
                    });
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to clean up old claim logs", e);
        }
    }

    public static synchronized void log(String message) {
        if (!enabled) return;

        rotateIfNeeded();

        String timestamp = LocalDateTime.now().format(LOG_TIME_FORMAT);
        String line = "[" + timestamp + "] " + message + System.lineSeparator();

        try {
            Files.writeString(currentLogFile, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to write to claim log file", e);
        }

        if (mirrorToConsole) {
            plugin.getLogger().info("[ClaimLogs] " + message);
        }
    }

    // ------------------------------------
    // Helpers
    // ------------------------------------

    private static synchronized String formatPlayer(Player player) {
        return player.getName() + " (" + player.getUniqueId() + ")";
    }

    // Claim creation
    public static synchronized void logClaimCreated(Player player, String claimName) {
        log(String.format("[CLAIM CREATED] %s created claim '%s'", formatPlayer(player), claimName));
    }

    // Claim deletion
    public static synchronized void logClaimDeleted(Player player, String claimName) {
        log(String.format("[CLAIM DELETED] %s deleted claim '%s'", formatPlayer(player), claimName));
    }

    // Protection change
    public static synchronized void logProtectionChanged(Player player, String claimName, String protectionName, String newValue) {
        log(String.format("[PROTECTION CHANGE] %s changed '%s' to '%s' in claim '%s'",
                formatPlayer(player), protectionName, newValue, claimName));
    }

    public static synchronized void logProtectionChanged(Player player, String claimName, Protection protection, String newValue) {
        logProtectionChanged(player, claimName, protection.getKey().toString(), newValue);
    }

    // Whitelist added
    public static synchronized void logWhitelistAdded(Player player, String claimName, Player target) {
        log(String.format("[WHITELIST ADDED] %s added %s (%s) to claim '%s'",
                formatPlayer(player), target.getName(), target.getUniqueId(), claimName));
    }

    // Whitelist removed
    public static synchronized void logWhitelistRemoved(Player player, String claimName, Player target) {
        log(String.format("[WHITELIST REMOVED] %s removed %s (%s) from claim '%s'",
                formatPlayer(player), target.getName(), target.getUniqueId(), claimName));
    }

    // Overloads for name/uuid-based targets (if target player isn't online)
    public static synchronized void logWhitelistAdded(Player player, String claimName, String targetName, String targetUUID) {
        log(String.format("[WHITELIST ADDED] %s added %s (%s) to claim '%s'",
                formatPlayer(player), targetName, targetUUID, claimName));
    }

    public static synchronized void logWhitelistRemoved(Player player, String claimName, String targetName, String targetUUID) {
        log(String.format("[WHITELIST REMOVED] %s removed %s (%s) from claim '%s'",
                formatPlayer(player), targetName, targetUUID, claimName));
    }
}
