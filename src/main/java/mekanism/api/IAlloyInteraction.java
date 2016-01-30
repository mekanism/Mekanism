package mekanism.api;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Implement this class in your TileEntity if it can interact with Mekanism alloys.
 * @author aidancbrady
 *
 */
public interface IAlloyInteraction 
{
	/**
	 * Called when a player right-clicks this block with an alloy.
	 * @param player - the player right-clicking the block
	 * @param tierOrdinal - the ordinal tier of the alloy (1 = advanced, 2 = elite, 3 = ultimate) 
	 */
	public void onAlloyInteraction(EntityPlayer player, int tierOrdinal);
}
