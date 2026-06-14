package net.mathias2246.buildmc.util;

import net.mathias2246.buildmc.api.claims.Claim;
import net.mathias2246.buildmc.api.claims.ClaimType;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LocationUtilTest {

    @Test
    void calculateChunkArea_singleChunk() {
        assertEquals(
                1,
                LocationUtil.calculateChunkArea(0, 0, 0, 0)
        );
    }

    @Test
    void calculateChunkArea_rectangle() {
        assertEquals(
                9,
                LocationUtil.calculateChunkArea(0, 0, 2, 2)
        );
    }

    @Test
    void calculateChunkArea_negativeCoordinates() {
        assertEquals(
                25,
                LocationUtil.calculateChunkArea(-2, -2, 2, 2)
        );
    }

    @Test
    void getBlockCorners_singleChunk() {

        Claim claim = new Claim(
                null,
                ClaimType.PLAYER,
                "owner",
                UUID.randomUUID(),
                0, 0,
                0, 0,
                "test",
                List.of(),
                List.of()
        );

        Vector[] corners = LocationUtil.getBlockCorners(claim);

        assertEquals(new Vector(0, 0, 0), corners[0]);
        assertEquals(new Vector(15, 0, 15), corners[1]);
    }

    @Test
    void getBlockCorners_multipleChunks() {

        Claim claim = new Claim(
                null,
                ClaimType.PLAYER,
                "owner",
                UUID.randomUUID(),
                0, 0,
                1, 1,
                "test",
                List.of(),
                List.of()
        );

        Vector[] corners = LocationUtil.getBlockCorners(claim);

        assertEquals(new Vector(0, 0, 0), corners[0]);
        assertEquals(new Vector(31, 0, 31), corners[1]);
    }

    @Test
    void getBlockCorners_unsortedChunks() {

        Claim claim = new Claim(
                null,
                ClaimType.PLAYER,
                "owner",
                UUID.randomUUID(),
                5, 5,
                2, 2,
                "test",
                List.of(),
                List.of()
        );

        Vector[] corners = LocationUtil.getBlockCorners(claim);

        assertEquals(new Vector(32, 0, 32), corners[0]);
        assertEquals(new Vector(95, 0, 95), corners[1]);
    }
}