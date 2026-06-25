package net.mathias2246.buildmc.api.messageBus;

@FunctionalInterface
public interface EventHandler {
    void handle(BusEvent event);
}