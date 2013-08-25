package universalelectricity.prefab.tile;

import net.minecraftforge.common.ForgeDirection;

/**
 * The interface is applied to TileEntities that can rotate.
 * 
 * @author Calclavia
 * 
 */

public interface IRotatable
{
	/**
	 * @return Gets the facing direction. Always returns the front side of the block.
	 */
	public ForgeDirection getDirection();

	/**
	 * @param Sets the facing direction.
	 */
	public void setDirection(ForgeDirection direection);
}
