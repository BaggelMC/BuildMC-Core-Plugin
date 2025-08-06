package net.mathias2246.buildmc.item.abstractTypes;

import net.mathias2246.buildmc.item.AbstractCustomItem;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractTool extends AbstractCustomItem {
    public AbstractTool(@NotNull Plugin plugin, @NotNull NamespacedKey key) {
        super(plugin, key);
    }
}
