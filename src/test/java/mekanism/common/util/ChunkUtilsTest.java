package mekanism.common.util;

import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Created by Thiakil on 2/05/2025.
 */
class ChunkUtilsTest {
    @Test
    @DisplayName("Test range closed on a single chunk range")
    void testSingle(){
        long[] longs = ChunkUtils.rangeClosed(1, 1, 1, 1);
        Assertions.assertArrayEquals(new long[]{ChunkPos.asLong(1,1)}, longs);
    }

    @Test
    @DisplayName("Test range closed on a dual chunk range on the X-axis")
    void testDualX(){
        long[] longs = ChunkUtils.rangeClosed(1, 1, 2, 1);
        Assertions.assertArrayEquals(new long[]{
              ChunkPos.asLong(1,1),
              ChunkPos.asLong(2,1)
        }, longs);
    }

    @Test
    @DisplayName("Test range closed on a dual chunk range on the Z-axis")
    void testDualZ(){
        long[] longs = ChunkUtils.rangeClosed(1, 1, 1, 2);
        Assertions.assertArrayEquals(new long[]{
              ChunkPos.asLong(1,1),
              ChunkPos.asLong(1,2)
        }, longs);
    }

    @Test
    @DisplayName("Test range closed on a dual chunk range on the X and Z axes")
    void testQuadXZ(){
        long[] longs = ChunkUtils.rangeClosed(1, 1, 2, 2);
        Assertions.assertArrayEquals(new long[]{
              ChunkPos.asLong(1,1),
              ChunkPos.asLong(2,1),
              ChunkPos.asLong(1,2),
              ChunkPos.asLong(2,2),
        }, longs);
    }

    @Test
    @DisplayName("Test range closed on a reversed dual chunk range on the X axis")
    void testQuadXZReverse(){
        long[] longs = ChunkUtils.rangeClosed(2, 2, 1, 1);
        Assertions.assertArrayEquals(new long[]{
              ChunkPos.asLong(2,2),
              ChunkPos.asLong(1,2),
              ChunkPos.asLong(2,1),
              ChunkPos.asLong(1,1),
        }, longs);
    }
}
