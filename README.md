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
  A secure, intuitive chunk claim system tied into the teams system. Built for balance and survival fairness.

- ⚙️ **Easy Setup**  
  Configure the plugin using `/buildmc` or edit the config files directly.

---

# For Server Owners:

If you're running your own server and want to use BuildMC-Core, here's how to install it:

### ✅ Requirements

- A **Minecraft 1.21.**\* server (Paper or Spigot recommended)
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
- Check out our guides for:
  - [Spawn Elytra](docs/usage/spawn_elytra.md)
  - [End Event](docs/usage/end_event.md)
- Compatible with most standard Paper/Spigot setups.
- May require a permission setup for players/admins using permission managers like [LuckPerms](https://luckperms.net/).

---

# For Developers:

## 🧑‍💻 Getting Started

### 🔧 Setup in IntelliJ IDEA

1. **Download Java 23+**  
   👉 [OpenJDK 23](https://jdk.java.net/java-se-ri/23)

2. **Get IntelliJ IDEA**  
   [https://www.jetbrains.com/idea/](https://www.jetbrains.com/idea/)

3. **Clone the Repo**  
   ```bash
   git clone https://github.com/BaggelMC/BuildMC-Core-Plugin.git
    ```

4. **Open in IntelliJ**

    * IntelliJ will auto-detect the Maven project.
    * Set your **Project SDK** to OpenJDK 23 (or later).

---

## 🐞 Debugging with `dt_socket`

Debug your local server like a pro! Here's how:

### 1️⃣ Start Server with Debug Enabled

```bash
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar paper.jar
```

* `address=*:5005` → Accepts debug connections on port 5005
* `suspend=n` → Server starts immediately (set `y` to wait for debugger)

---

### 2️⃣ Configure IntelliJ

1. Go to `Run` → `Edit Configurations...`
2. Click `+` → **Remote JVM Debug**
3. Fill in:

    * **Name:** `Local Server Debug`
    * **Host:** `localhost`
    * **Port:** `5005`
4. Save and apply.

---

### 3️⃣ Debug!

* Start your server with debug flags
* In IntelliJ, run the **Local Server Debug** config
* Set breakpoints and interact with the server
* IntelliJ will pause on your breakpoints as expected

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

---

## ⚖️ License

This project is licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).

---

## 📌 Repository

👉 [GitHub – BaggelMC/BuildMC-Core-Plugin](https://github.com/BaggelMC/BuildMC-Core-Plugin)

---

*Thanks for building with us! 🧱✨*
