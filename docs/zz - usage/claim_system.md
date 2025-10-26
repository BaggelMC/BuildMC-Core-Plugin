# 📦 Claim System

This document explains how the **claim system** works in the plugin and how players and admins can use it effectively.

---

## 🧰 Getting Started

To claim areas, you need the **Claim Tool**. Use the following command to get it:

```plaintext
/claim claimtool
```

Claims are always in **chunks**. You cannot claim areas smaller than a full chunk. If you do so, the entire chunk will be claimed automatically.

---

## 📐 How to Claim Land

1. **Stand in the first corner** chunk of the area you want to claim.
2. **Click with the claim tool** (either left-click or right-click).
3. **Go to the opposite corner** of your desired claim area.
4. **Sneak and click** (hold shift and click) with the tool to set the second position.

✅ If done correctly, the area will be claimed **for your entire team**.

> ⚠️ Only players in a team can claim land. Solo players cannot make claims.

---

## 🤝 Whitelisting Players

Want to allow players outside your team to access your claims?

Use:

```plaintext
/claim whitelist add <player>
```

This grants them access to your claimed chunks.

---

## ❌ Removing a Claim

To remove a claimed area:

1. Use the **remove tool**:

   ```plaintext
   /claim removetool
   ```
2. Repeat the same corner selection process (click → sneak+click).

---

## 🔍 Who Owns This Chunk?

Use the command below to check the owner of the chunk you're standing in:

```plaintext
/claim who
```

---

## 🔧 Admin Configuration

Below are relevant settings found in the plugin's config file:

### General Claim Settings

```yaml
claims:
  enabled: true                  # Enables the claim system
  save-on-world-save: true       # Saves claim data on world save
  max-chunk-claim-amount: 1024   # Max chunks a team can claim
```

### Claim Tool Settings

```yaml
  tool:
    tool-item: "carrot_on_a_stick"     # The item used as the claim tool
    enable-give-command: true          # Allows any player to use /buildmc claimtool
    limit-selection: 8                 # Max selection size (in chunks)
```

### Protections Within Claims

```yaml
  protections:
    player-break: true                # Blocks cannot be broken
    player-place: true                # Blocks cannot be placed
    containers: true                  # Containers (e.g., chests) cannot be opened

    explosion-block-damage: true      # Blocks protected from explosions
    explosion-entity-damage: true     # Entities protected from explosions

    damage:
      entity-damage: true             # Players can't damage protected entities
      exclude-players: true           # Players can still hurt each other
```

---

## 🛡️ Notes

* **All claims are team-based.** Only team members can claim and remove claims.