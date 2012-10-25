package railcraft.common.api.carts.bore;

import net.minecraft.src.EntityMinecart;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;

/**
 * This interface can be implemented by a block class to control whether a block can be
 * mined by the bore without having to force the user to edit the configuration file.
 *
 * If the block is found to implement this class, any setting in the configuration
 * is ignored for that block.
 *
 * Generally, the reason blocks are not minable by default is to prevent you
 * from intentionally or accidentally boring through your base.
 *
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public interface IMineable
{

    /**
     * Called when the Bore attempts to mine the block. If it returns false,
     * the Bore will halt operation.
     *
     * @param world The World
     * @param i x-Coord
     * @param j y-Coord
     * @param k z-Coord
     * @param bore The Bore entity
     * @param head The BoreHead, item implements IBoreHead.
     * @return true if mineable
     * @see IBoreHead
     */
    public boolean canMineBlock(World world, int i, int j, int k, EntityMinecart bore, ItemStack head);
}
