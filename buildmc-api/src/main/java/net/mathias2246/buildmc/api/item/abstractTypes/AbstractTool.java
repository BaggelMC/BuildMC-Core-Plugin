package net.mathias2246.buildmc.api.item.abstractTypes;

import net.mathias2246.buildmc.api.item.AbstractCustomItem;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**An abstract class that should be used to define custom tools for your plugin.*/
public abstract class AbstractTool extends AbstractCustomItem {

    public AbstractTool(@NotNull Plugin plugin, @NotNull NamespacedKey key) {
        super(plugin, key);
    }

}
