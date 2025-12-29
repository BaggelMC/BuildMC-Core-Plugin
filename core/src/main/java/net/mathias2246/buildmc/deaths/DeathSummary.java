package net.mathias2246.buildmc.deaths;

public record DeathSummary(
        long id,
        long timestamp,
        int xp,
        String cause
) {}