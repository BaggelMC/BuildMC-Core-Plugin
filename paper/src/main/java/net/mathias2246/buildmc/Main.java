package net.mathias2246.buildmc;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.mathias2246.buildmc.claims.*;
import net.mathias2246.buildmc.claims.listeners.*;
import net.mathias2246.buildmc.commands.BuildMcCommand;
import net.mathias2246.buildmc.endEvent.EndListener;
import net.mathias2246.buildmc.spawnElytra.DisableBoostListener;
import net.mathias2246.buildmc.spawnElytra.ElytraZoneManager;
import net.mathias2246.buildmc.spawnElytra.SpawnBoostListener;
import net.mathias2246.buildmc.status.PlayerStatus;
import net.mathias2246.buildmc.status.SetStatusCommand;
import net.mathias2246.buildmc.status.StatusConfig;
import net.mathias2246.buildmc.util.Message;
import net.mathias2246.buildmc.util.Sounds;
import net.mathias2246.buildmc.util.language.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.logging.Logger;

public final class Main extends JavaPlugin implements MainClass {

    public static Logger logger;

    public static Plugin plugin;

    @Subst("")
    public static FileConfiguration config;
    public static File configFile;

    public static File pluginFolder;

    public static final ElytraZoneManager zoneManager = new ElytraZoneManager();

    public static StatusConfig statusConfig;

    public static ClaimManager claimManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        logger = this.getLogger();

        pluginFolder = plugin.getDataFolder();
        if (!pluginFolder.exists()) {
            //noinspection ResultOfMethodCallIgnored
            pluginFolder.mkdir();
        }

        CoreMain.initialize(this);

        LanguageManager.init();

        configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) this.saveResource("config.yml", false);
        config = this.getConfig();

        EndListener.loadFromConfig();

        Sounds.setup();
        ClaimTool.setup();

        getServer().getPluginManager().registerEvents(new EndListener(), this);

        getServer().getPluginManager().registerEvents(new ClaimTool(), this);

        if (config.getBoolean("spawn-elytra.enabled")) {
            getServer().getPluginManager().registerEvents(new SpawnBoostListener(zoneManager), this);
            if (config.getBoolean("spawn-elytra.disable-rockets")) getServer().getPluginManager().registerEvents(new DisableBoostListener(), this);

            zoneManager.loadZoneFromConfig();
        }

        if (config.getBoolean("claims.enabled")) {

            if (config.getBoolean("claims.protections.containers")) {
                getServer().getPluginManager().registerEvents(new ClaimContainerListener(), this);
            }

            if (config.getBoolean("claims.protections.damage.explosion-block-damage") || config.getBoolean("claims.protections.explosion-entity-damage")) {
                getServer().getPluginManager().registerEvents(new ClaimExplosionsListener(), this);
            }

            if (config.getBoolean("claims.protections.player-break")) {
                getServer().getPluginManager().registerEvents(new ClaimBreakListener(), this);
            }

            if (config.getBoolean("claims.protections.player-place")) {
                getServer().getPluginManager().registerEvents(new ClaimPlaceListener(), this);
            }

            if (config.getBoolean("claims.protections.damage.entity-damage")) {
                getServer().getPluginManager().registerEvents(new ClaimDamageProtectionListener(), this);
            }

            if (config.getBoolean("claims.protections.sign-editing")) {
                getServer().getPluginManager().registerEvents(new ClaimSignEditListener(), this);
            }

            if (config.getBoolean("claims.protections.prevent-interactions")) {
                getServer().getPluginManager().registerEvents(new ClaimInteractionListener(), this);
            }

            if (config.getBoolean("claims.protections.splash-potions")) {
                getServer().getPluginManager().registerEvents(new ClaimPotionSplashListener(), this);
            }

            if (config.getBoolean("claims.protections.vehicle-enter")) {
                getServer().getPluginManager().registerEvents(new ClaimVehicleEnterListener(), this);
            }

            if (config.getBoolean("claims.protections.bucket-usage")) {
                getServer().getPluginManager().registerEvents(new ClaimBucketUseEvent(), this);
            }

            if (config.getBoolean("claims.protections.prevent-entity-modifications")) {
                getServer().getPluginManager().registerEvents(new ClaimEntityChangeBlockListener(), this);
            }

            if (config.getBoolean("claims.protections.item-pickup")) {
                getServer().getPluginManager().registerEvents(new ClaimItemPickupListener(), this);
            }

            if (config.getBoolean("claims.protections.item-drop")) {
                getServer().getPluginManager().registerEvents(new ClaimItemDropListener(), this);
            }

            if (config.getBoolean("claims.protections.frostwalker")) {
                getServer().getPluginManager().registerEvents(new ClaimFrostWalkerListener(), this);
            }

            if (config.getBoolean("claims.protections.piston-movement-across-claim-borders")) {
                getServer().getPluginManager().registerEvents(new ClaimPistonMovementListener(), this);
            }

        }

        if (config.getBoolean("status.enabled")) {
            statusConfig = new StatusConfig(this);
            getServer().getPluginManager().registerEvents(new PlayerStatus(), this);
        }

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(new BuildMcCommand().getCommand());

            if (config.getBoolean("status.enabled")) commands.registrar().register(new SetStatusCommand(statusConfig).getCommand());

            if (config.getBoolean("claims.enabled")) commands.registrar().register(new ClaimCommand().getCommand());
        });


        if (config.getBoolean("disable-commands")) {
            if (config.isList("disabled-commands")) {
                for (String fullCommand : config.getStringList("disabled-commands")) {
                    String[] parts = fullCommand.split(":", 2);
                    if (parts.length == 2) {
                        disableCommand(parts[0], parts[1]);
                    } else {
                        logger.warning("Invalid command format in 'disabled-commands': " + fullCommand);
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        CoreMain.stop();
    }

    private void disableCommand(String namespace, String commandName) {
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());

            if (!(commandMap instanceof SimpleCommandMap)) {
                logger.warning("Unsupported CommandMap implementation. Cannot disable command: " + commandName);
                return;
            }

            // Cast to access knownCommands
            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);

            // Remove all relevant keys
            knownCommands.keySet().removeIf(key ->
                    key.equalsIgnoreCase(commandName) ||
                            key.equalsIgnoreCase(namespace + ":" + commandName) ||
                            key.endsWith(":" + commandName));

            // Register dummy override
            Command blockedCommand = new Command(commandName) {
                @Override
                public boolean execute(@NotNull CommandSender sender, @NotNull String label, String @NotNull [] args) {
                    sender.sendMessage(Message.msg(sender, "messages.error.command-disabled"));
                    return true;
                }
            };

            commandMap.register(namespace, blockedCommand);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.warning("Failed to fully disable command '" + commandName + "': " + e.getMessage());
        }
    }


    private CommandMap getCommandMap() {
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            return (CommandMap) commandMapField.get(Bukkit.getServer());
        } catch (Exception e) {
            logger.warning("Failed to retrieve the command map.");
            return null;
        }
    }

    @Override
    public void sendPlayerMessage(Player player, Component message) {
        player.sendMessage(message);
    }

    @Override
    public void sendPlayerActionBar(Player player, Component message) {
        player.sendActionBar(message);
    }
}
