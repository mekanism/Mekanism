package cofh.api.tileentity;

import cofh.api.block.IBlockDebug;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Implement this interface on Tile Entities which can be debugged via some in-game method, such as a tool. The containing block should be an instance of
 * {@link IBlockDebug} and defer the call to the tile.
 * 
 * @author King Lemming
 * 
 */
public interface ITileDebug {

	/**
	 * This function debugs a tile entity.
	 * 
	 * @param side
	 *            The side of the block.
	 * @param player
	 *            Player doing the debugging.
	 */
	void debugTile(ForgeDirection side, EntityPlayer player);

}
