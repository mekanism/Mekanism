package railcraft.common.api.carts;

import net.minecraft.src.Block;

/**
 * Used by the renderer to renders blocks in carts.
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public interface ICartRenderInterface
{

    /**
     * Return the block that should be rendered in the cart.
     * @return The Block to render
     */
    public Block getBlock();

    /**
     * Return the metadata for the block
     * that should be rendered in the cart.
     * @return metadata
     */
    public int getBlockMetadata();
}
