package net.mathias2246.buildmc.util;

/**An Enum containing all the keys of the registries defined by BuildMC.*/
public enum DefaultRegistries {

    /**The default protection registry key*/
    PROTECTIONS ("protections");

    @Override
    public String toString() {
        return s;
    }

    final String s;

    DefaultRegistries(String string) {
        s = string;
    }
}
