# Custom Permissions

---

## General
We use custom permissions to give admins and certain players ways to manage things like e.g. claims, etc.<p>
Custom permissions can be used by admins to bypass claim protections, or open the end (see [End-Event](https://github.com/BaggelMC/BuildMC-Core-Plugin/blob/master/docs/usage/end_event.md))
> 🛑 You should never give someone you don't trust permissions as they might abuse them.

### Permissions

| Permission                 | Usages                                                                            |
|----------------------------|-----------------------------------------------------------------------------------|
| `buildmc.admin`            | Allows you to do anything important                                               |
| `buildmc.bypass-claims`    | You aren't affected by any claim protections                                      |
| `buildmc.force-claim`      | You can remove or overwrite any claim. (only use if you really need to)           |
| `buildmc.bypass-end-event` | With this permission you can enter the end dimension even when the end is closed. |


## ➡️ Applying and removing

Our plugin doesn't have a built-in way of setting custom permissions, so you have to use a permission manager plugin like [LuckPerms](https://luckperms.net/).
<p>
To add a permission to a player use the command:

> /lp user \<Player Name\> permission set \<Permission\>

To remove a permission from a player, use the command:

> /lp user \<Player Name\> permission unset \<Permission\>


