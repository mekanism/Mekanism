package mekanism.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;

public class ChunkUtils {

    /// Adapted from [ChunkPos#rangeClosed] to avoid Stream city
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
            positions[i++] = ChunkPos.pack(posX, posZ);
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

    /// [ChunkPos#pack(BlockPos)] but with a packed Block pos
    ///
    /// @param packedBlock packed blockpos ([BlockPos#asLong()])
    ///
    /// @return a packed Chunk pos
    public static long packedBlockToChunk(long packedBlock) {
        return ChunkPos.pack(SectionPos.blockToSectionCoord(BlockPos.getX(packedBlock)), SectionPos.blockToSectionCoord(BlockPos.getZ(packedBlock)));
    }
}
