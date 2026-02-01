package net.mathias2246.buildmc.commands.debug;

import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.claims.ClaimType;
import net.mathias2246.buildmc.claims.ClaimManager;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BenchmarkClaims {

    public static int benchmark1000Claims() {
        List<Claim> claims = new ArrayList<>();

        UUID worldId = Bukkit.getWorlds().getFirst().getUID();

        for (int i = 0; i < 1000; i++) {
            claims.add(
                    new Claim(
                            null,
                            ClaimType.SERVER,
                            "server",
                            worldId,
                            0,
                            i,
                            0,
                            i,
                            "TEST "+i,
                            new ArrayList<>(),
                            new ArrayList<>()
                    )
            );
        }
        try {
            ClaimManager.registerClaims(claims);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return 1;
    }

}
