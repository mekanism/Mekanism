package ic2.api;

import net.minecraft.world.World;

/**
 * Allows a block to be painted by a Painter.
 */
public interface IPaintableBlock
{
	/**
	 * Color the block.
	 * 
	 * @param world block's world
	 * @param x block X position
	 * @param y block Y position
	 * @param z block Z position
	 * @param color painter color, same as dye metadata values
	 * @return Whether the block was painted and the painter should be damaged
	 */
	public boolean colorBlock(World world, int x, int y, int z, int color);
}
