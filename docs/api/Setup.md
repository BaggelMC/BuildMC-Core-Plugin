
# Setup


<!-- TODO: Finish after creating BuildMC-Core-API maven repo --> 

BuildMC-Core has a public API that you can use to build plugins that are able to work and communicate with our plugins code and extend it.

There are a few steps that are required when creating a plugin that uses our API.

## Add Dependency


## How to use

### 🧠 What Is BuildMcRegistryEvent?

The [BuildMcRegistryEvent](../../buildmc-api/src/main/java/net/mathias2246/buildmc/api/BuildMcAPI.java) is part of the BuildMC lifecycle.
It is triggered once the BuildMC API is ready to be modified or extended, but before BuildMC starts handling player data, claims, or world events.

You can think of it as a *“setup phase”* where:</br>
Registries, configurations, and systems are initialized.

Extensions can intercept, modify, or replace internal components before BuildMC is finalized.

During this event, you can access the BuildMcAPI instance through event.getApi() and use it to modify BuildMC’s behavior programmatically.

### ⚙️ Implementation Example

Here’s how to use BuildMcRegistryEvent to access and modify the BuildMC API in your own plugin:
````java
public class MyExtensionPlugin extends JavaPlugin implements Listener {

    // Store the BuildMcAPI instance for use in other parts of your code
    public static BuildMcAPI buildmcApi;
    
    @Override
    public void onEnable() {
        // Register this class as a listener so it can receive the BuildMcRegistryEvent
        getServer().getPluginManager().registerEvents(this, this);
    }

    /**
     * This method is called when BuildMC-Core fires its registry event.
     * You can access and modify the API from here.
     */
    @EventHandler
    public void onRegistry(BuildMcRegistryEvent event) {
        // Obtain the BuildMC API instance
        buildmcApi = event.getApi();

        // Use the API to modify BuildMC's configuration or registry entries, 
        // For example: disable the claims system by editing the configuration
        buildmcApi.editConfiguration(config -> config.set("claims.enabled", false));

        // You could also register new systems, change permissions,
        // or add custom logic hooks before BuildMC fully initializes.
    }
}
````

---

###### <div><p align="left">Visit us on [Baggel.de](https://baggel.de)! 🥯<p/><p align="right">[Licensed under the Apache License, Version 2.0](https://github.com/BaggelMC/BuildMC-Core-Plugin/blob/master/LICENSE)<p/><div/>
