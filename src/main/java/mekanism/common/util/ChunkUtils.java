package mekanism.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;

public class ChunkUtils {

    /**
     * Adapted from {@link ChunkPos#rangeClosed} to avoid Stream city
     */
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

    /**
     * {@link ChunkPos#asLong(BlockPos)} but with a packed Block pos
     *
     * @param packedBlock packed blockpos ({@link BlockPos#asLong()})
     *
     * @return a packed Chunk pos
     */
    public static long packedBlockToChunk(long packedBlock) {
        return ChunkPos.asLong(SectionPos.blockToSectionCoord(BlockPos.getX(packedBlock)), SectionPos.blockToSectionCoord(BlockPos.getZ(packedBlock)));
    }
}
