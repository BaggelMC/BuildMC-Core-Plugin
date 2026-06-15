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

    @Test
    void blockToChunk_origin() {
        assertEquals(0, LocationUtil.blockToChunk(0));
    }

    @Test
    void blockToChunk_lastBlockOfChunk() {
        assertEquals(0, LocationUtil.blockToChunk(15));
    }

    @Test
    void blockToChunk_firstBlockOfNextChunk() {
        assertEquals(1, LocationUtil.blockToChunk(16));
    }

    @Test
    void blockToChunk_negativeBlock() {
        // Block -1 is in chunk -1
        assertEquals(-1, LocationUtil.blockToChunk(-1));
    }

    @Test
    void blockToChunk_negativeChunkBoundary() {
        assertEquals(-1, LocationUtil.blockToChunk(-16));
    }

    @Test
    void normalizeChunkCoords_alreadySorted() {
        assertArrayEquals(
                new int[]{1, 2, 5, 6},
                LocationUtil.normalizeChunkCoords(1, 2, 5, 6)
        );
    }

    @Test
    void normalizeChunkCoords_reversed() {
        assertArrayEquals(
                new int[]{1, 2, 5, 6},
                LocationUtil.normalizeChunkCoords(5, 6, 1, 2)
        );
    }

    @Test
    void normalizeChunkCoords_mixedAxes() {
        // X is reversed, Z is not
        assertArrayEquals(
                new int[]{-3, 0, 4, 7},
                LocationUtil.normalizeChunkCoords(4, 0, -3, 7)
        );
    }

    @Test
    void normalizeChunkCoords_samePoint() {
        assertArrayEquals(
                new int[]{3, 3, 3, 3},
                LocationUtil.normalizeChunkCoords(3, 3, 3, 3)
        );
    }

    @Test
    void normalizeChunkCoords_claim_sortedCoords() {
        Claim claim = makeClaim(0, 0, 3, 3);
        assertArrayEquals(new int[]{0, 0, 3, 3}, LocationUtil.normalizeChunkCoords(claim));
    }

    @Test
    void normalizeChunkCoords_claim_reversedCoords() {
        Claim claim = makeClaim(5, 5, 1, 1);
        assertArrayEquals(new int[]{1, 1, 5, 5}, LocationUtil.normalizeChunkCoords(claim));
    }

    @Test
    void calculateChunkArea_swappedCorners_sameResultAsOrdered() {
        // Order of corners must not affect the result
        assertEquals(
                LocationUtil.calculateChunkArea(0, 0, 3, 4),
                LocationUtil.calculateChunkArea(3, 4, 0, 0)
        );
    }

    @Test
    void calculateChunkArea_singleRow() {
        assertEquals(5, LocationUtil.calculateChunkArea(0, 0, 4, 0));
    }

    @Test
    void calculateChunkArea_singleColumn() {
        assertEquals(5, LocationUtil.calculateChunkArea(0, 0, 0, 4));
    }

    @Test
    void getBlockCorners_negativeChunks() {
        Claim claim = makeClaim(-2, -2, -1, -1);
        Vector[] corners = LocationUtil.getBlockCorners(claim);
        assertEquals(new Vector(-32, 0, -32), corners[0]);
        assertEquals(new Vector(-1, 0, -1), corners[1]);
    }

    @Test
    void getBlockCorners_crossesOrigin() {
        Claim claim = makeClaim(-1, -1, 0, 0);
        Vector[] corners = LocationUtil.getBlockCorners(claim);
        assertEquals(new Vector(-16, 0, -16), corners[0]);
        assertEquals(new Vector(15, 0, 15), corners[1]);
    }

    @Test
    void getBlockCorners_returnsExactlyTwoVectors() {
        Vector[] corners = LocationUtil.getBlockCorners(makeClaim(0, 0, 0, 0));
        assertEquals(2, corners.length);
    }

    // helpers
    private static Claim makeClaim(int x1, int z1, int x2, int z2) {
        return new Claim(
                null,
                ClaimType.PLAYER,
                "owner",
                UUID.randomUUID(),
                x1, z1,
                x2, z2,
                "test",
                List.of(),
                List.of()
        );
    }
}