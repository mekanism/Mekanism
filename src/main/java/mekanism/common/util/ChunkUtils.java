package mekanism.common.util;

import net.minecraft.world.level.ChunkPos;

/**
 * Created by Thiakil on 2/05/2025.
 */
public class ChunkUtils {

    //adapted from ChunkPos.rangeClosed
    public static long[] rangeClosed(final int startX, int startZ, final int endX, final int endZ) {
        int sizeX = Math.abs(startX - endX) + 1;
        int sizeZ = Math.abs(startZ - endZ) + 1;
        int arrSize = sizeX * sizeZ;
        long[] positions = new long[arrSize];
        final int dirX = startX < endX ? 1 : -1;
        final int dirZ = startZ < endZ ? 1 : -1;

        int posX = startX;
        int posZ = startZ;
        int i = 0;

        do {
            positions[i++] = ChunkPos.asLong(posX, posZ);
            int prevX = posX;
            int prevZ = posZ;
            if (prevX == endX) {
                if (prevZ == endZ) {
                    break;
                }

                posX = startX;
                posZ = prevZ + dirZ;
            } else {
                posX = prevX + dirX;
                //posZ = prevZ;
            }
        } while (i < arrSize);

        return positions;
    }
}
