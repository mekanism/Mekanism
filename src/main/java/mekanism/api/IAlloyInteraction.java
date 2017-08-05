package mekanism.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

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
	 * @param hand - the hand this alloy was right-clicked with
	 * @param stack - the stack of alloy being right-clicked
	 * @param tierOrdinal - the ordinal tier of the alloy (1 = advanced, 2 = elite, 3 = ultimate) 
	 */
    void onAlloyInteraction(EntityPlayer player, EnumHand hand, ItemStack stack, int tierOrdinal);
}
