package mekanism.common.base;

import javax.annotation.Nonnull;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3i;

/**
 * Internal interface.  A bounding block is not actually a 'bounding' block, it is really just a fake block that is used to mimic actual block bounds.
 *
 * @author AidanBrady
 */
public interface IBoundingBlock {

    /**
     * Called when the main block is placed.
     */
    void onPlace();

    /**
     * Called when any part of the structure is broken.
     */
    void onBreak();

    /**
     * Used for getting the proper BlockFaceShape for the bounding block.
     *
     * @param face   The face of the block at the offset that the shape is needed for.
     * @param offset Offset from the implementation of IBoundingBlock
     *
     * @return A BlockFaceShape
     */
    @Nonnull
    default BlockFaceShape getOffsetBlockFaceShape(@Nonnull Direction face, @Nonnull Vec3i offset) {
        return BlockFaceShape.UNDEFINED;
    }
}