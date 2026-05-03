package net.mathias2246.buildmc.util;

import net.mathias2246.buildmc.CoreMain;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.MultiLineChart;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

public final class BStats {
    private static final int pluginId = 27021;

    public static void initialize() {
        Metrics metrics = new Metrics(CoreMain.plugin, pluginId);

        metrics.addCustomChart(new MultiLineChart("servers_and_players", () -> {
            Map<String, Integer> values = new HashMap<>();
            values.put("Servers", 1); // Assuming only 1 server is running per plugin instance.
            values.put("Players", Bukkit.getOnlinePlayers().size());
            return values;
        }));
    }
}
