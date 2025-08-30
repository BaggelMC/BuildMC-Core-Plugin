package net.mathias2246.buildmc.util;

public enum DefaultRegistries {

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
