# 🌀 End Event System

The **End Event System** in BuildMC-Core is designed to delay endgame progression, promote server-wide participation, and ensure a memorable **first dragon fight**. It helps create anticipation, encourages community play, and supports a longer server lifespan by introducing **scheduled content**.

---

## 🎯 Purpose

* ❌ Prevents early access to the **End** dimension.
* 🗓️ Allows admins to open the End at the right moment for a **community event**.
* 🔄 Keeps your server’s progression healthy and **content-relevant** over time.

---

## 🛠️ Commands

Admins can **toggle access to the End** dynamically. **No restart required**:

```plaintext
/buildmc endevent close
```

> 🚫 Closes the End. Prevents **all entities** (players, mobs, TNT, etc.) from entering via end portals.

```plaintext
/buildmc endevent open
```

> ✅ Opens the End. All end portals behave normally again.

> ℹ️ A broadcast message is sent to all players whenever the End is toggled.

---

## ⚙️ Configuration Options

Located in:
📄 `config.yml` → `end-event` section

| Key                        | Description                                                                                                                                                                    |
|----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `disallow-tnt-through-end` | Prevents **TNT**, **TNT minecarts**, and **Creepers** from traveling through End portals. Helps prevent **spawn griefing** via cross-dimensional explosions. *(Default: true)* |

---

## 📌 How It Works

* When the **End is closed**, **all End portals are disabled** for both players and entities.
* Players attempting to enter are simply **blocked**, creating a clean and predictable behavior.
* **TNT protection** ensures players can't use complex redstone to break into spawn areas or grief the End or spawn.

---

## 🧠 Recommended Usage

* 🕐 **Disable the End** at server launch to slow down progression.
* 📣 **Schedule a server-wide event** (e.g. “End Opening Day”) where everyone joins for the **first dragon fight**.

---

## 💬 Example Use Case

> 🗓️ "The End will open this Saturday at 6PM UTC. Be there for the first Ender Dragon battle and loot!"

Players log in, coordinate, and participate, creating **memorable moments** and renewed interest in the server.

---

🌀 *The End Event system is simple but powerful. Use it to shape your server’s pacing and turn the End into an exciting, shared milestone.* 🐉
