# 🛡️ Protections

The [`Protection`](../../buildmc-api/src/main/java/net/mathias2246/buildmc/api/claims/Protection.java) class defines a **type of protection** that can be applied to BuildMC claims.  
A protection represents a rule or behavior that restricts or modifies player interactions in a claim — for example, disabling block breaking, preventing explosions, or blocking PVP.

Each protection:
- Has a unique [`NamespacedKey`](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/NamespacedKey.html)
- Can be **registered** in the BuildMC registry
- May be **enabled by default** when new claims are created
- May be **hidden from players** so they cannot be changed by players
- Can contain **custom Bukkit event handlers**, because it implements the `Listener` interface

---

## 📦 Class Overview

```java
public abstract class Protection implements Keyed, Displayable, Listener
```

**Implemented interfaces:**
- `Keyed` — provides a unique `NamespacedKey` for identification.
- `Displayable` — allows integration with BuildMC’s UI system (translation and display text).
- `Listener` — lets you directly define Bukkit event handlers inside your protection.

---

## ⚙️ Creating a Custom Protection

To implement a new protection type, simply extend the abstract `Protection` class and implement the required methods.

Example: A protection that disables **block breaking** inside claims.

```java
import net.mathias2246.buildmc.api.claims.Protection;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

public class NoBlockBreakProtection extends Protection {

    private final NamespacedKey key;

    public NoBlockBreakProtection(NamespacedKey key) {
        this.key = key;
        this.isDefaultEnabled = true; // Is enabled by default when creating new claims
        this.isHidden = false; // Is not hidden from the player
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }

    @Override
    public String getTranslationBaseKey() {
        // Used for UI translations (e.g., "protection.no_block_break")
        return "protection.no_block_break";
    }

    /**
     * Example, Bukkit event listener.
     * This will automatically be registered if your plugin registers this protection as a Listener.
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Example: prevent block breaking in protected claims
        if (/* check if the player is in a claim where this protection is active */) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("Block breaking is disabled in this area!");
        }
    }
}
```

---

## 🧩 Registering the Protection

To make your protection available in BuildMC, register it via the `DeferredRegistry<Protection>` that BuildMC provides in the [`BuildMcRegistryEvent`](../../buildmc-api/src/main/java/net/mathias2246/buildmc/api/event/lifecycle/BuildMcRegistryEvent.java).

> **IMPORTANT**: You need to register every Protection inside the `BuildMcRegistryEvent` or else an exception is thrown!

Example registration:

```java
import net.mathias2246.buildmc.api.event.lifecycle.BuildMcRegistryEvent;
import net.mathias2246.buildmc.api.claims.Protection;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class MyProtectionPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Register this class as an event listener
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onRegistry(BuildMcRegistryEvent event) {
        // Tries to get the Protections Register from BuildMC
        api.getRegistriesHolder().getOptional(
                DefaultRegistries.PROTECTIONS.toString()
        ).ifPresent( // If the Protections register exists, add a new instance of the NoBlockBreakProtection
                protectionsRegistry -> 
                        protectionsRegistry.addEntries(
                            new NoBlockBreakProtection(NamespacedKey.fromString("my_plugin:no_block_breaking"))
                        )
        );
    }
}
```

---

## 💡 Event Handling Inside Protections

Because `Protection` **implements `Listener`**, you can directly handle **any Bukkit event** inside your protection implementation — no need for a separate listener class.

For example, you can listen to multiple events in the same protection:

```java
 // Cancel entity explosions
@EventHandler
public void onExplosion(EntityExplodeEvent event) {
    // Prevent explosions inside protected claims
    if (/* claim check */) {
        event.blockList().clear();
    }
}

// Cancel entity damage
@EventHandler
public void onPlayerDamage(EntityDamageByEntityEvent event) {
    // Prevent PVP inside protected claims
    if (/* claim check */) {
        event.setCancelled(true);
    }
}
```

This makes each protection **self-contained**, handling its own logic and event registration in one class.

---

## 🧠 Best Practices

- Each `Protection` should handle **one core responsibility** (e.g., “No PVP”, “No Explosions”).
- Use `getTranslationBaseKey()` to provide a unique translation key for UI display.
- Use the provided `DeferredRegistry` to register protections instead of static initialization.

---

###### <div><p align="left">Visit us on [Baggel.de](https://baggel.de)! 🥯<p/><p align="right">[Licensed under the Apache License, Version 2.0](https://github.com/BaggelMC/BuildMC-Core-Plugin/blob/master/LICENSE)<p/><div/>