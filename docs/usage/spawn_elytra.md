# 🪂 Spawn Elytra System

The **Spawn Elytra Boost** feature allows players in **Survival** or **Adventure** mode to launch into the sky from a defined spawn area, without the need for rockets. This system ensures fair, limited-use mobility and is ideal for hub areas.

---

## 📍 Setting Up the Elytra Zone

To enable the feature, you’ll first need to **define a zone** (referred to as the *elytra zone*) where the boost will be active.

### ✅ Step-by-Step Setup

1. **Choose an area** in your world where players should be able to activate the boost (e.g., a spawn platform).
2. **Mark the two opposite corners** of the zone using this command:

```plaintext
/buildmc elytrazone setup pos1 ~ ~ ~
/buildmc elytrazone setup pos2 ~ ~ ~
```

> Replace `~ ~ ~` with your desired coordinates or use the command while standing in the correct location.

3. Once both positions are set, the **elytra zone becomes active**. That’s it! Players in *Survival* or *Adventure* mode can now launch using Elytra within this region.

---

## ⚙️ Advanced Configuration

Further customization is available in the plugin’s configuration file:

### 📄 `config.yml` → `spawn-elytra` section

Here’s what you can configure:

| Key                 | Description                                                                                                             |
|---------------------|-------------------------------------------------------------------------------------------------------------------------|
| `enabled`           | Enables/disables the entire Spawn Elytra feature. *(Default: true)*                                                     |
| `strength`          | Boost power (velocity multiplier). <br>**1** = no boost, **5** = default BuildMC value.                                 |
| `disable-rockets`   | Blocks players from using **firework rockets** during flight. <br>Set to `true` to **prevent abuse**. *(Default: true)* |
| `zone`              | Manually define or adjust the `world`, `pos1`, and `pos2` if needed. Useful for fine-tuning after setup.                |

You can changen translations and messages for the spawn-elytra under `lang/some-language.yml` → `messages.spawn-elytra`.


> ❗ **Riptide Note**: Players may still use Riptide tridents for additional mobility. This behavior cannot currently be disabled due to Riptide being processed on the client-side.

---

## 🧪 Troubleshooting

* **Players not launching?**

    * Make sure they are in *Survival* or *Adventure* mode.
    * Verify that the elytra zone is correctly defined.
    * Confirm the feature is `enabled` in the config.

* **Are zone changes not taking effect?**

    * Reload the plugin or restart the server after editing `config.yml`.

---

🧱 *The Spawn Elytra system helps players take off with style—without breaking survival balance. Use it to elevate your world’s experience!* ✨
