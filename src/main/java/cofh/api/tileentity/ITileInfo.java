package cofh.api.tileentity;

import cofh.api.block.IBlockInfo;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Implement this interface on Tile Entities which can provide information about themselves. The containing block should be an instance of {@link IBlockInfo}
 * and defer the call to the tile.
 * 
 * @author King Lemming
 * 
 */
public interface ITileInfo {

	/**
	 * This function appends information to a list provided to it.
	 * 
	 * @param info
	 *            The list that the information should be appended to.
	 * @param side
	 *            The side of the block that is being queried.
	 * @param player
	 *            Player doing the querying - this can be NULL.
	 * @param debug
	 *            If true, the tile should return "debug" information.
	 */
	void getTileInfo(List<String> info, ForgeDirection side, EntityPlayer player, boolean debug);

}
