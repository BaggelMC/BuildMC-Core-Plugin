package net.mathias2246.buildmc.util;

import org.bukkit.Keyed;

import java.util.Map;

public interface Group extends Keyed {
    String getId();
    Map<String, Boolean> getPermissions();
}
