package net.mathias2246.buildmc.util.registry;

/**An Enum containing all the keys of the registries defined by BuildMC.*/
public enum DefaultRegistries {

    /**The default protection registry key*/
    PROTECTIONS ("protections"),
    /// The default statuses registry key
    STATUSES ("statuses"),
    /// The default custom-item registry
    CUSTOM_ITEMS ("custom_items"),
    /** The default guides registry **/
    GUIDES ("guides"),
    /** The event channel registry **/
    EVENT_CHANNELS ("event_channels"),
    /** The event subscription registry **/
    EVENT_SUBSCRIPTIONS ("event_subscriptions");

    @Override
    public String toString() {
        return s;
    }

    final String s;

    DefaultRegistries(String string) {
        s = string;
    }
}
