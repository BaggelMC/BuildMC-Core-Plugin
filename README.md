# 🌍 BuildMC-Core

*A powerful, core plugin for the BuildMC SMP project. Made for modern Minecraft development, crafted with care for 1.21.*\*

---

## ✨ Features

BuildMC-Core provides a solid foundation for any Minecraft project. Designed for the **BuildMC SMP**, it bundles essential features every project might need:

- 🧑‍🤝‍🧑 **Teams & Player Status**  
  Create and manage teams and let players set their status.

- 🪂 **Spawn Elytra Boost**  
  Launch yourself from spawn with a built-in Elytra boost system. Rocket usage is blocked to prevent abuse.

- 🌀 **Toggle The End**  
  Enable or disable the End dimension **dynamically**. No restarts required. Broadcasts the change to all players.

- 🧱 **Survival-Friendly Chunk Claiming**  
  A secure, intuitive chunk claim system tied into the team system. Built for balance and survival fairness.

- 💀 **Death-Restoring**  
  Allow Moderators to restore deaths of their players.

- ⚙️ **Easy Setup**  
  Configure the plugin using `/buildmc` or edit the config files directly.

---

# For Server Owners:

If you're running your own server and want to use BuildMC-Core, here's how to install it:

### ✅ Requirements

- A **Minecraft 1.21.**\* server (Paper is recommended or Spigot)
- Java **17+** (Java 23 recommended)
- BuildMC-Core plugin `.jar` file

### 📥 Installation Steps

1. **Download the Plugin**
    - Head to the [Releases](https://github.com/BaggelMC/BuildMC-Core-Plugin/releases) page.
    - Download the latest `BuildMC-Core-x.x.x.jar` file.

2. **Install it on Your Server**
    - Place the `.jar` file into your server’s `plugins/` directory.
    - Start (or restart) your server.

3. **Configure (Optional)**
    - Run `/buildmc` in-game for setup options.
    - Or modify the `BuildMC-Core/config.yml` file directly.

### 💡 Notes
- Check out our guides on our [website](https://baggel.de/plugins/en/core/).
- Compatible with most standard Paper/Spigot setups.
- May require a permission setup for players/admins using permission managers like [LuckPerms](https://luckperms.net/).

---

# For Developers

## 🧩 Using the BuildMC API

BuildMC-Core exposes a public API that allows other plugins to integrate seamlessly with the BuildMC SMP ecosystem.

The API is published as a **separate Maven artifact** and is intended to be used with **`provided` scope**, since the core plugin supplies it at runtime.

---

## 📦 Adding the API as a Dependency

### Maven

```xml
<repository>
    <id>central</id>
    <url>https://central.sonatype.com/repository/maven-snapshots/</url>
</repository>

<dependency>
    <groupId>net.mathias2246</groupId>
    <artifactId>buildmc-api</artifactId>
    <version>1.1.0-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

> ⚠️ **Important:**
> Do **not** shade or relocate the API.
> BuildMC-Core must be installed on the server.

---

## 🚀 Getting Started with the API

### 🔹 Accessing the API Instance

The API becomes available **after BuildMC-Core has finished loading**.
To safely access it, listen to the lifecycle event:

```
net.mathias2246.buildmc.api.event.lifecycle.BuildMcFinishedLoadingEvent
```

This event provides access to the main API entry point:

```
net.mathias2246.buildmc.api.BuildMcAPI
```

### Example Flow

1. Register a listener in your plugin
2. Listen for `BuildMcFinishedLoadingEvent`
3. Retrieve and store the `BuildMcAPI` instance
4. Use the API throughout your plugin

This ensures:

* BuildMC-Core is fully initialized
* All internal systems (teams, claims, status, etc.) are ready

---

## 🧠 API Entry Points

The central access point for all integrations is:

```
net.mathias2246.buildmc.api.BuildMcAPI
```

From there, you can interact with:

* Team systems
* Player status handling
* Chunk claiming logic
* Lifecycle-aware features provided by BuildMC-Core

(See the JavaDocs or source for details on available managers and services.)

---

## 🔗 Plugin Compatibility

* Requires **BuildMC-Core** to be installed on the server
* Compatible with **Paper / Spigot 1.21+**
* Designed for **modular expansion**, not standalone usage

---

## 🤝 Contributing

We welcome **issues**, **pull requests**, and thoughtful contributions.

However, please note:

> **BuildMC-Core is tailored to the BuildMC SMP environment and is not a general-purpose plugin.**

If you'd like to expand or adapt it, feel free to **fork the repository**.

---

## 👥 Authors

* **Mathias2246**
* **Darkyl**
* **ShxdowCrafter**

---

## ⚖️ License

This project is licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).

---

## 📌 Repository

👉 [GitHub – BaggelMC/BuildMC-Core-Plugin](https://github.com/BaggelMC/BuildMC-Core-Plugin)

---

*Thanks for building with us! 🧱✨*
