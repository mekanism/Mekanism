package mekanism.common.util;

import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by Thiakil on 2/05/2025.
 */
public class ChunkUtilsTest {
    @Test
    void testSingle(){
        long[] longs = ChunkUtils.rangeClosed(1, 1, 1, 1);
        Assertions.assertArrayEquals(new long[]{ChunkPos.asLong(1,1)}, longs);
    }

    @Test
    void testDualX(){
        long[] longs = ChunkUtils.rangeClosed(1, 1, 2, 1);
        Assertions.assertArrayEquals(new long[]{
              ChunkPos.asLong(1,1),
              ChunkPos.asLong(2,1)
        }, longs);
    }

    @Test
    void testDualZ(){
        long[] longs = ChunkUtils.rangeClosed(1, 1, 1, 2);
        Assertions.assertArrayEquals(new long[]{
              ChunkPos.asLong(1,1),
              ChunkPos.asLong(1,2)
        }, longs);
    }

    @Test
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
