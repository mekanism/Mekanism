package universalelectricity.prefab.implement;

import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

/**
 * The interface is applied to Blocks and TileEntities that can rotate.
 * 
 * @author Calclavia
 * 
 */

public interface IRotatable
{
	/**
	 * @return Gets the facing direction. Always returns the front side of the block.
	 */
	public ForgeDirection getDirection(IBlockAccess world, int x, int y, int z);

	/**
	 * @param Sets the facing direction.
	 */
	public void setDirection(World world, int x, int y, int z, ForgeDirection facingDirection);
}
