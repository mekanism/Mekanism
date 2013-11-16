package universalelectricity.prefab.block;

import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

/** The interface is applied to Blocks that can rotate.
 *
 * @author DarkGuardsman */

public interface IRotatableBlock
{
    /** @return Gets the facing direction. Always returns the front side of the block. */
    public ForgeDirection getDirection(World world, int x, int y, int z);

    /** @param Sets the facing direction. */
    public void setDirection(World world, int x, int y, int z, ForgeDirection direection);
}
