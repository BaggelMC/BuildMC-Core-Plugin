package net.mathias2246.buildmc.platform;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.mathias2246.buildmc.util.language.LanguageManager;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.util.Arrays;
import java.util.Locale;

public class BetterBungeeComponentSerializer {

    private static final BungeeComponentSerializer defaultSerializer = BungeeComponentSerializer.get();

    public static BaseComponent serialize(Component component, Locale locale) {

        if (component instanceof TranslatableComponent translatableComponent) {
            Component t = LanguageManager.getTranslator().translate(translatableComponent, locale);
            if (t != null) component = t;
        }
        Component[] children = component.children().toArray(new Component[] {});
        for (int i = 0; i < children.length; i++) {
            var c = children[i];
            if (c instanceof TranslatableComponent translatableComponent) {
                Component t = LanguageManager.getTranslator().translate(translatableComponent, locale);
                if (t != null) c = t;
            }
            children[i] = c;
        }

        component = component.children(Arrays.asList(children));

        return ComponentSerializer.deserialize(GsonComponentSerializer.gson().serialize(component));
    }
}
