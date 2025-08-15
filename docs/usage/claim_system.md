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

> ⚙️ As of patch 1.0.3, you can change if the player should shift-click or just right-click.
>  <br>That's because some might be used to other tools working like that

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
  # If true, players can manage their team claims using '/claim ...'.
  # Admins can also set certain regions as server-claims that no one can do anything in
  enabled: true # Default: true

  # If true, all claim data will be writen to disk when the server saves the overworld.
  # Only change this to false if you really need to
  save-on-world-save: true # Default: true

  # The amount of chunks each team can claim
  max-chunk-claim-amount: 1024 # Default: 1024
```

### Claim Tool Settings

```yaml
      # Settings related to the claim-tool. The claim-tool (if enabled) can be obtained using '/buildmc claimtool'
      tool:
         # The item used for the claim-tool
         tool-item: "carrot_on_a_stick" # Default: "carrot_on_a_stick"

         # If true, the selection and remove tool will require the player to right-click instead of sneak-clicking to set the second position.
         # NOTE: This will not change the '/claim help' text or the lore of the claim selection and remove tool.
         #       You have to manually change them inside the language files.
         use-right-instead-of-sneak-click: false # Default: false

         # If true, players even without permissions can get the claim-tools using '/claim claimtool' or '/claim removetool'
         enable-give-command: true # Default: true

         # Limits the maximum size in chunks of the selection.
         # The limit is disabled if lower than zero.
         limit-selection: 8 # Default: 8
```

### Protections Within Claims

```yaml
    # What aspects are protected when claimed
    protections:
       # Players cannot break blocks
       player-break: true # Default: true
       # Players cannot place blocks
       player-place: true # Default: true

       # Players cannot open containers (e.g. Chests, Barrels...)
       # Players are still able to open their ender chests or read books in lecterns.
       # But players can't take books from lecterns in others claims.
       containers: true # Default: true
      
      # There are much more setting than this. But it would be way too much...
```


---

## 🛡️ Note

_This system will be completely overwritten in the future..._