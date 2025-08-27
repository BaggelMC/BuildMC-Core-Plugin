package net.mathias2246.buildmc.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LocationUtilTest {

    @Test
    void calculateChunkArea_WithSameChunk_ShouldReturnOne() {
        int result = LocationUtil.calculateChunkArea(0, 0, 0, 0);
        assertEquals(1, result);
    }

    @Test
    void calculateChunkArea_WithAdjacentChunks_ShouldReturnFour() {
        int result = LocationUtil.calculateChunkArea(0, 0, 1, 1);
        assertEquals(4, result);
    }

    @Test
    void calculateChunkArea_WithSwappedCoordinates_ShouldReturnSame() {
        int result1 = LocationUtil.calculateChunkArea(0, 0, 2, 2);
        int result2 = LocationUtil.calculateChunkArea(2, 2, 0, 0);
        assertEquals(result1, result2);
        assertEquals(9, result1);
    }

    @Test
    void calculateChunkArea_WithNegativeCoordinates_ShouldReturnCorrectArea() {
        int result = LocationUtil.calculateChunkArea(-2, -2, 1, 1);
        assertEquals(16, result); // 4 by 4 chunks
    }

    @Test
    void calculateChunkArea_WithSameLocation_ShouldReturnOne() {
        World mockWorld = mock(World.class);

        Location loc1 = mock(Location.class);
        when(loc1.getWorld()).thenReturn(mockWorld);
        when(loc1.getBlockX()).thenReturn(0);
        when(loc1.getBlockZ()).thenReturn(0);

        Location loc2 = mock(Location.class);
        when(loc2.getWorld()).thenReturn(mockWorld);
        when(loc2.getBlockX()).thenReturn(15); // still in chunk 0
        when(loc2.getBlockZ()).thenReturn(15); // still in chunk 0

        int result = LocationUtil.calculateChunkArea(loc1, loc2);
        assertEquals(1, result);
    }

    @Test
    void calculateChunkArea_WithDifferentChunks_ShouldReturnCorrectArea() {
        World mockWorld = mock(World.class);

        Location loc1 = mock(Location.class);
        when(loc1.getWorld()).thenReturn(mockWorld);
        when(loc1.getBlockX()).thenReturn(0);
        when(loc1.getBlockZ()).thenReturn(0);

        Location loc2 = mock(Location.class);
        when(loc2.getWorld()).thenReturn(mockWorld);
        when(loc2.getBlockX()).thenReturn(32); // chunk X = 2
        when(loc2.getBlockZ()).thenReturn(32); // chunk Z = 2

        int result = LocationUtil.calculateChunkArea(loc1, loc2);
        assertEquals(9, result); // 3x3 chunks
    }

    @Test
    void calculateChunkArea_WithDifferentWorlds_ShouldThrowException() {
        World world1 = mock(World.class);
        World world2 = mock(World.class);

        Location loc1 = mock(Location.class);
        when(loc1.getWorld()).thenReturn(world1);

        Location loc2 = mock(Location.class);
        when(loc2.getWorld()).thenReturn(world2);

        assertThrows(IllegalArgumentException.class, () -> LocationUtil.calculateChunkArea(loc1, loc2));
    }
}
