# BuildMC Event System

The BuildMC API introduces a powerful and extensible **event system** built on top of Bukkit’s native event architecture.  
This system provides additional functionality for metadata management and simplifies integration with BuildMC’s internal lifecycle and claim systems.

---

## 📚 Overview

Custom events in the BuildMC API are designed to allow plugin developers to **hook into BuildMC’s behavior** without directly modifying its source code.  
Developers can listen to these events to **observe, modify, or cancel** plugin operations such as claim creation, removal, player entry, and more.

---

## 🧩 Event Metadata

The BuildMC API provides a flexible metadata system through the [`MetadataHolder`](../../buildmc-api/src/main/java/net/mathias2246/buildmc/api/event/MetadataHolder.java) interface.  
This feature enables developers to attach **custom key–value pairs** to events at runtime.

### Example

```java
event.putMetadata("myplugin.debug", true);
boolean debug = (boolean) event.getMetadata("myplugin.debug");
```

This makes it easy to share contextual data between event listeners, or between different plugins handling the same event.

---

## ⚙️ CustomEvent

All BuildMC custom events extend the abstract [`CustomEvent`](../../buildmc-api/src/main/java/net/mathias2246/buildmc/api/event/CustomEvent.java) class.

```java
public abstract class CustomEvent extends Event {
    protected final MetadataHolder metadataHolder = new MetadataHolder() {};

    @NotNull
    public Map<String, Object> getMetadata() {
        return metadataHolder.getMetadata();
    }

    public void putMetadata(@NotNull String key, @NotNull Object value) {
        metadataHolder.putMetadata(key, value);
    }

    public void removeMetadata(@NotNull String key) {
        metadataHolder.removeMetadata(key);
    }
}
```

### 🔍 Features

- ✅ Built-in metadata support
- ✅ Standard Bukkit `Event` compliance
- ✅ Simple to extend and implement
- ✅ Allows plugins to attach data dynamically

### 💡 Implementing a Custom Event

If you want to create your own event that integrates with this system:

```java
public class MyCustomEvent extends CustomEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;

    public MyCustomEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
```

---

## 📜 List of BuildMC Events

Below is a list of all **custom events** currently available in the BuildMC API.

| Event Name                            | Cancellable | Description |
|--------------------------------------|--------------|--------------|
| **ClaimCreateEvent**                 | ✅ Yes       | Fired when a claim is created. Can be cancelled to prevent creation. |
| **ClaimRemoveEvent**                 | ❌ No        | Fired when a claim is removed. |
| **ClaimChangeEvent**                 | ❌ No        | Fired when a claim’s properties change. |
| **PlayerEnterClaimEvent**            | ❌ No        | Fired when a player enters a claim. |
| **PlayerLeaveClaimEvent**            | ❌ No        | Fired when a player leaves a claim. |
| **EndStateChangeEvent**              | ✅ Yes       | Fired when the server or plugin changes the end state. |
| **PlayerHeadDropEvent**              | ✅ Yes       | Fired when a player’s head is dropped on death. |
| **PlayerSpawnTeleportPreConditionEvent** | ✅ Yes   | Fired before teleporting a player to spawn. Can be used to block or modify teleportation. |

---

## 🧠 Example: Listening to an Event

Here’s an example of listening to a BuildMC event using Bukkit’s event system:

```java
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import net.mathias2246.buildmc.api.event.claims.PlayerEnterClaimEvent;

public class ExampleListener implements Listener {

    @EventHandler
    public void onPlayerEnterClaim(PlayerEnterClaimEvent event) {
        event.getPlayer().sendMessage("You have entered a claim!");

        // Example: add metadata for tracking
        event.putMetadata("enteredByPlugin", true);
    }
}
```

---

## 🧩 Example: Using Metadata in an Event

```java
@EventHandler
public void onClaimCreate(ClaimCreateEvent event) {
    // Add metadata to the event for later reference
    event.putMetadata("createdByPlugin", "MyCustomPlugin");

    // Check if creation is allowed
    if (!event.getPlayer().hasPermission("claims.create")) {
        event.setCancelled(true);
        event.getPlayer().sendMessage("You don't have permission to create claims.");
    }
}
```

---

## ✅ Summary

The BuildMC event system extends Bukkit’s event model with:

- A **metadata framework** for flexible data sharing
- A **consistent base event type (`CustomEvent`)**
- A suite of **high-level game and system events** for easy plugin integration

This allows developers to safely and cleanly integrate their plugins with BuildMC’s internals — no reflection or source modification required.

---


---

###### <div><p align="left">Visit us on [Baggel.de](https://baggel.de)! 🥯<p/><p align="right">[Licensed under the Apache License, Version 2.0](https://github.com/BaggelMC/BuildMC-Core-Plugin/blob/master/LICENSE)<p/><div/>
